package io.kyligence.notebook.console.service;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.EngineAccessException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.scalalib.hint.HintManager;
import io.kyligence.notebook.console.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EngineService {

    private static NotebookConfig config = NotebookConfig.getInstance();

    @Autowired
    private RestTemplate restTemplate;

    public String execute(RunScriptParams params, String url){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        applySqlHint(params);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        params.getAll().forEach(map::add);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.postForEntity( url, request , String.class);
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

    public String runScript(RunScriptParams params) {
        String url = config.getExecutionEngineUrl() + "/run/script";
        return execute(params, url);
    }

    public String runAnalyze(RunScriptParams params) {
        String url = config.getExecutionEngineUrl() + "/run/script?executeMode=analyze";
        return execute(params, url);
    }

    public String runAutoSuggest(RunScriptParams params) {
        String url = config.getExecutionEngineUrl() + "/run/script?executeMode=autoSuggest";
        return execute(params, url);
    }

    private void applySqlHint(RunScriptParams runScriptParams) {
        String newSql = HintManager.applyHintRewrite(runScriptParams.params.get("sql"), runScriptParams.params);
        runScriptParams.withSql(newSql);
    }

    public static class  RunScriptParams {

        private Map<String, String> params = new HashMap<>();
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
            params.put("schemaInferUrl", config.getExecutionEngineUrl() + "/run/script");
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

            String username = null;
            try {
                username = WebUtils.getCurrentLoginUser();
            } catch (Exception e) {
                log.error("error when get username");
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

        public RunScriptParams withSql(String sql) {
            params.put("sql", sql);
            return this;
        }

        public RunScriptParams withAsync(String async) {
            params.put("async", async);
            return this;
        }

        public Map<String, String> getAll() {
            return this.params;
        }

    }



}
