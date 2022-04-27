package io.kyligence.notebook.console.service;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.bean.model.EngineResource;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.EngineAccessException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.scalalib.hint.HintManager;
import io.kyligence.notebook.console.util.EngineStatus;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import org.springframework.data.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EngineService {

    private static final NotebookConfig config = NotebookConfig.getInstance();

    private final Map<String, String> engineMap = new HashMap<>();

    private final Map<String, Pair<EngineStatus, Double>> engineStatusMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "EngineStatusRefresher");
        t.setDaemon(true);
        return t;
    });

    public interface EngineAPI {
        String RUN_SCRIPT = "/run/script";
        String RUN_ANALYZE = "/run/script?executeMode=analyze";
        String RUN_SUGGEST = "/run/script?executeMode=autoSuggest";
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SystemService systemService;

    @PostConstruct
    public void initEngineStatusRefresher() {
        log.info("Starting EngineStatusRefresher...");
        // init engineMap
        engineMap.put("default", config.getByzerEngineUrl());
        engineMap.put("backup", config.getByzerEngineBackupUrl());

        Runnable checkEngineStatus = () -> engineMap.keySet().forEach(
                engine -> engineStatusMap.put(engine, getEngineStatusAndUsage(engine)));

        // refresh at start
        checkEngineStatus.run();

        // refresh every 15 seconds
        this.executor.scheduleWithFixedDelay(checkEngineStatus, 15, 15, TimeUnit.SECONDS);
        log.info("Schedule EngineStatusRefresher every 15 seconds");
    }

    private String execute(RunScriptParams params, String url, SqlHint sqlHint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        sqlHint.apply();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        params.getAll().forEach(map::add);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ByzerException(ErrorCodeEnum.ENGINE_ERROR, response.getBody());
            }
        } catch (ByzerException e) {
            throw e;
        } catch (Exception e) {
            throw new EngineAccessException(ErrorCodeEnum.ENGINE_ACCESS_EXCEPTION, e);
        }

        return response.getBody();
    }

    public Pair<EngineStatus, Double> getEngineStatusAndUsage(String engine) {
        if (!engineIsAlive(engine) || !engineIsReady(engine)) {
            return Pair.of(EngineStatus.DOWN, 0.);
        }
        return Pair.of(EngineStatus.READY, getEngineResourceUsage(engine));
    }

    public Pair<EngineStatus, Double> getEngineStatusAndUsage(String engine, boolean refresh) {
        engine = Objects.isNull(engine) ? getExecutionEngine() : engine;
        if (refresh) {
            engineStatusMap.put(engine, getEngineStatusAndUsage(engine));
        }
        return engineStatusMap.get(engine);
    }

    public String runScript(RunScriptParams params) {
        Pair<String, RunScriptParams> rewritten = rewriteWithUserConfig(params);
        String url = rewritten.getFirst() + EngineAPI.RUN_SCRIPT;
        return execute(rewritten.getSecond(), url, () -> {
            String newSql = HintManager.applyAllHintRewrite(params.params.get("sql"), params.params);
            params.withSql(newSql);
        });
    }

    public String runScript(RunScriptParams params, String engine) {
        String url = engineMap.getOrDefault(engine, config.getExecutionEngineUrl()) + EngineAPI.RUN_SCRIPT;
        return execute(params.withSchemaInferUrl(url), url, () -> {
            String newSql = HintManager.applyAllHintRewrite(params.params.get("sql"), params.params);
            params.withSql(newSql);
        });
    }

    public String runAnalyze(RunScriptParams params) {
        Pair<String, RunScriptParams> rewritten = rewriteWithUserConfig(params);
        String url = rewritten.getFirst() + EngineAPI.RUN_ANALYZE;
        return execute(rewritten.getSecond(), url, () -> {
            String newSql = HintManager.applyNoEffectRewrite(params.params.get("sql"), params.params);
            params.withSql(newSql);
        });
    }

    public String runAutoSuggest(RunScriptParams params) {
        Pair<String, RunScriptParams> rewritten = rewriteWithUserConfig(params);
        String url = rewritten.getFirst() + EngineAPI.RUN_SUGGEST;
        return execute(rewritten.getSecond(), url, () -> {
            String newSql = HintManager.applyNoEffectRewrite(params.params.get("sql"), params.params);
            params.withSql(newSql);
        });
    }

    public String getExecutionEngine() {
        try {
            String username = WebUtils.getCurrentLoginUser();
            SystemConfig userConfig = systemService.getConfig(username);
            return engineMap.containsKey(userConfig.getEngine()) ?
                    userConfig.getEngine() : config.getExecutionEngine();
        } catch (Exception e) {
            return config.getExecutionEngine();
        }
    }

    public List<String> getEngineList() {
        return new ArrayList<>(engineMap.keySet());
    }

    public Map<String, Pair<EngineStatus, Double>> getEngineStatusMap() {
        return engineStatusMap;
    }

    public boolean isReady(String engine) {
        return engineIsAlive(engine) && engineIsReady(engine);
    }

    private Pair<String, RunScriptParams> rewriteWithUserConfig(RunScriptParams params) {
        try {
            String username = WebUtils.getCurrentLoginUser();
            SystemConfig userConfig = systemService.getConfig(username);
            String engineUrl = engineMap.getOrDefault(userConfig.getEngine(), config.getExecutionEngineUrl());
            return Pair.of(engineUrl, params.withTimeout(userConfig.getTimeout() <= 0 ? -1 : userConfig.getTimeout() * 60 * 1000)
                    .withSchemaInferUrl(engineUrl + EngineAPI.RUN_SCRIPT));
        } catch (Exception e) {
            return Pair.of(config.getByzerEngineUrl(), params);
        }
    }

    private Double getEngineResourceUsage(String engine) {
        String resp = runScript(new EngineService.RunScriptParams()
                .withAsync("false")
                .withOwner("admin")
                .withSql("!show resource;"), engine);
        List<EngineResource> engineResourceList = JacksonUtils.readJsonArray(resp, EngineResource.class);
        if (Objects.nonNull(engineResourceList) && !engineResourceList.isEmpty()) {
            EngineResource engineResource = engineResourceList.get(0);
            return Math.min((double) engineResource.getActiveTasks() / (double) engineResource.getTotalCores(), 1.0);
        }
        return 0.0;
    }

    private void query(String url) {
        ResponseEntity<String> response;

        try {
            response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ByzerException(ErrorCodeEnum.ENGINE_ERROR, response.getBody());
            }
        } catch (ByzerException e) {
            throw e;
        } catch (Exception e) {
            throw new EngineAccessException(ErrorCodeEnum.ENGINE_ACCESS_EXCEPTION, e);
        }
    }

    private boolean engineIsAlive(String engine) {
        String url = engineMap.getOrDefault(engine, config.getExecutionEngineUrl())
                + "/health/liveness";
        try {
            query(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean engineIsReady(String engine) {
        String url = engineMap.getOrDefault(engine, config.getExecutionEngineUrl())
                + "/health/readiness";
        try {
            query(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class RunScriptParams {

        private final Map<String, String> params = new HashMap<>();

        {
            params.put("jobName", UUID.randomUUID().toString());
            params.put("owner", "admin");
            params.put("tags", "");
            params.put("access_token", "mlsql");
            params.put("skipGrammarValidate", "false");
            params.put("timeout", config.getExecutionTimeoutMillonSeconds());
            params.put("engine-name", "mlsql-engine");
            params.put("show_stack", "true");
            params.put("sql", "!hdfs -ls /;");
            params.put("schemaInferUrl", config.getExecutionEngineUrl() + EngineAPI.RUN_SCRIPT);
            params.put("context.__default__include_fetch_url__", config.getNotebookUrl() + "/api/script/include");
            params.put("context.__default__console_url__", config.getNotebookUrl());
            params.put("context.__default__fileserver_url__", config.getNotebookUrl() + "/api/upload_file");
            params.put("context.__default__fileserver_upload_url__", config.getNotebookUrl() + "/api/upload_file");
            params.put("context.__auth_client__", config.getAuthClient());
            params.put("context.__auth_server_url__", config.getNotebookUrl() + "/table/auth");
            params.put("context.__auth_secret__", "mlsql");
            params.put("skipAuth", "false");
            params.put("callback", config.getNotebookUrl() + "/api/job/callback");
            params.put("async", "true");
            params.put("sessionPerUser", "true");
            params.put("defaultPathPrefix", "/mlsql");
            params.put("home", config.getUserHome());
            params.put("maxRetries", String.valueOf(config.getExecutionEngineCallbackRetries() - 1));

            String username = null;
            try {
                username = WebUtils.getCurrentLoginUser();
            } catch (Exception e) {
                log.debug("error when get username");
            }
            if (username != null) {
                params.put("owner", username.toLowerCase());
                params.put("defaultPathPrefix", config.getUserHome() + "/" + username);
            }
        }

        public RunScriptParams with(String key, String value) {
            params.put(key, value);
            return this;
        }

        public RunScriptParams withJobName(String jobName) {
            params.put("jobName", jobName);
            return this;
        }

        public RunScriptParams withOwner(String owner) {
            params.put("owner", owner);
            return this;
        }

        public RunScriptParams withOwnerPathPrefix(String owner) {
            params.put("defaultPathPrefix", config.getUserHome() + "/" + owner);
            return this;
        }

        public RunScriptParams withSql(String sql) {
            params.put("sql", sql);
            return this;
        }

        public RunScriptParams withAsync(String async) {
            params.put("async", async);
            return this;
        }

        public RunScriptParams withLimit(String limit) {
            params.put("outputSize", limit);
            return this;
        }

        public RunScriptParams withIncludeSchema(Boolean includeSchema) {
            params.put("includeSchema", includeSchema.toString());
            return this;
        }

        public RunScriptParams withTimeout(long timeout) {
            params.put("timeout", String.valueOf(timeout));
            return this;
        }

        public RunScriptParams withSchemaInferUrl(String url) {
            params.put("schemaInferUrl", url);
            return this;
        }

        public Map<String, String> getAll() {
            return this.params;
        }

    }

    @PreDestroy
    public void shutdownEngineStatusRefresher() {
        log.info("Shutdown EngineStatusRefresher");
        this.executor.shutdownNow();
    }
}
