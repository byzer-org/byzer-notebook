package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.bean.dto.ConnectionDTO;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.dao.ConnectionInfoRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
import io.kyligence.notebook.console.util.ConnectionUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;


@Service
@Slf4j
public class ConnectionService {
    @Autowired
    private ConnectionInfoRepository connectionInfoRepository;

    @Autowired
    private EngineService engineService;

    @Autowired
    private CriteriaQueryBuilder criteriaQueryBuilder;

    private final Map<String, Map<Integer, Boolean>> connectionStatus = new HashMap<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ConnectionStatusRefresher");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void initConnectionStatusRefresher() {
        log.info("Starting ConnectionStatusRefresher...");

        // init connection status map for each engine
        engineService.getEngineList().forEach(engine -> connectionStatus.put(engine, new ConcurrentHashMap<>()));

        Runnable checkAllConnection = () -> engineService.getEngineList().forEach(this::refreshAllConnections);
        // refresh at start
        checkAllConnection.run();

        // refresh every 5 minutes
        this.executor.scheduleWithFixedDelay(checkAllConnection, 5, 5, TimeUnit.MINUTES);
        log.info("Schedule ConnectionStatusRefresher every 5 minutes");
    }

    public ConnectionInfo findById(Integer id) {
        return connectionInfoRepository.findById(id).orElse(null);
    }

    public boolean testConnection(ConnectionDTO content) {
        if (content.getName() == null) content.setName("UserConnectionTmp");
        String sql = ConnectionUtils.renderSQL(content);
        return checkConnection(sql, content.getName(), engineService.getExecutionEngine());
    }

    public boolean testConnection(ConnectionInfo info, String engine) {
        return checkConnection(renderConnectionSQL(info), info.getName(), engine);
    }

    public String renderConnectionSQL(ConnectionInfo info) {
        List<ConnectionDTO.ParameterMap> parameter = new ArrayList<>();
        if (info.getParameter() != null) {
            parameter = JacksonUtils.readJsonArray(info.getParameter(), ConnectionDTO.ParameterMap.class);
        }
        return ConnectionUtils.renderSQL(
                info.getUrl(), info.getDriver(), info.getUserName(),
                info.getPassword(), info.getName(), parameter
        );
    }

    public List<String> showTables(Integer connectionId) {
        ConnectionInfo connectionInfo = findById(connectionId);
        if (connectionInfo == null) {
            throw new ByzerException(ErrorCodeEnum.CONNECTION_NOT_EXIST);
        }
        return showConnectionTables(connectionInfo);
    }

    private List<String> showConnectionTables(ConnectionInfo info) {
        String sql = String.format(
                "run command as JDBC.`%1$s._` where" +
                        "`driver-statement-query`=\"show tables\"" +
                        "and sqlMode=\"query\"" +
                        "as %1$s_show_tables;",
                info.getName()
        );
        String result = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .with("skipAuth", "true")
                        .withAsync("false")
        );
        List<Map> tableList = JacksonUtils.readJsonArray(result, Map.class);
        List<String> tables = new ArrayList<>();
        if (tableList != null) {
            tableList.forEach(map -> map.values().forEach(v -> tables.add(v.toString())));
        }
        return tables;
    }

    private boolean checkConnection(String renderedSQL, String connectionName, String engine) {
        EngineService.RunScriptParams runScriptParams = new EngineService.RunScriptParams();

        try {
            engineService.runScript(
                    runScriptParams
                            .withSql(renderedSQL)
                            .withAsync("false"),
                    engine
            );
            String testSQL = String.format(
                    "run command as JDBC.`%1$s._` where" +
                            "`driver-statement-query`=\"select 1\"" +
                            "and sqlMode=\"query\"" +
                            "as %1$s_show_tables;",
                    connectionName
            );
            String result = engineService.runScript(
                    runScriptParams.withSql(testSQL)
                            .with("skipAuth", "true")
                            .withAsync("false")
                            .withTimeout(500)
            );
            List<Map> parsed = JacksonUtils.readJsonArray(result, Map.class);
            return parsed != null;
        } catch (Exception e) {
            return false;
        }
    }


    @Transactional
    public ConnectionInfo createConnection(String user, ConnectionDTO content) {
        if (!testConnection(content)) {
            throw new ByzerException(ErrorCodeEnum.CONNECTION_REFUSED);
        }

        if (connectionNameExist(user, content.getName())) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_CONNECTION_NAME);
        }

        long currentTimeStamp = System.currentTimeMillis();
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setDatasource(content.getDatasource());
        connectionInfo.setUser(user);
        connectionInfo.setName(content.getName());
        connectionInfo.setDriver(content.getDriver());
        connectionInfo.setUrl(content.getUrl());
        connectionInfo.setUserName(content.getUserName());
        connectionInfo.setPassword(content.getPassword());
        connectionInfo.setParameter(JacksonUtils.writeJson(content.getParameter()));

        connectionInfo.setCreateTime(new Timestamp(currentTimeStamp));
        connectionInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        connectionInfo = connectionInfoRepository.save(connectionInfo);

        if (connectionInfo.getName() == null) {
            connectionInfo.setName("UserConnection_" + connectionInfo.getId());
            connectionInfoRepository.updateConnectionName(connectionInfo.getId(), connectionInfo.getName());
        }
        return connectionInfo;
    }

    @Transactional
    public ConnectionInfo updateConnection(Integer connectionId, String user, ConnectionDTO content) {
        permissionCheck(connectionId, user);

        if (!testConnection(content)) {
            throw new ByzerException(ErrorCodeEnum.CONNECTION_REFUSED);
        }

        ConnectionInfo connectionInfo = findById(connectionId);
        if (!connectionInfo.getName().equals(content.getName()) && connectionNameExist(user, content.getName())) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_CONNECTION_NAME);
        }

        long currentTimeStamp = System.currentTimeMillis();
        ConnectionInfo newConnectionInfo = new ConnectionInfo();
        newConnectionInfo.setId(connectionId);
        newConnectionInfo.setDatasource(content.getDatasource());
        newConnectionInfo.setUser(user);
        if (content.getName() != null) {
            newConnectionInfo.setName(content.getName());
        }
        newConnectionInfo.setDriver(content.getDriver());
        newConnectionInfo.setUrl(content.getUrl());
        newConnectionInfo.setUserName(content.getUserName());
        newConnectionInfo.setPassword(content.getPassword());
        newConnectionInfo.setParameter(JacksonUtils.writeJson(content.getParameter()));
        newConnectionInfo.setCreateTime(connectionInfo.getCreateTime());
        newConnectionInfo.setUpdateTime(new Timestamp(currentTimeStamp));

        updateByConnectionId(newConnectionInfo);

        return newConnectionInfo;
    }

    @Transactional
    public void updateByConnectionId(ConnectionInfo connectionInfo) {
        Query query = criteriaQueryBuilder.updateNotNullByField(connectionInfo, "id");
        query.executeUpdate();
    }

    public boolean connectionNameExist(String user, String name) {
        if (name == null) return false;
        return connectionInfoRepository.findByUserAndName(user, name).size() > 0;
    }

    @Transactional
    public void deleteConnection(Integer connectionId, String user) {
        permissionCheck(connectionId, user);
        connectionInfoRepository.deleteById(connectionId);
        connectionStatus.remove(connectionId);
    }

    public void permissionCheck(Integer connectionId, String user) {
        ConnectionInfo connectionInfo = findById(connectionId);
        if (connectionInfo == null) {
            throw new ByzerException(ErrorCodeEnum.CONNECTION_NOT_EXIST);
        }
        if (!user.equalsIgnoreCase(connectionInfo.getUser())) {
            throw new ByzerException(ErrorCodeEnum.CONNECTION_NOT_AVAILABLE);
        }
    }

    public List<ConnectionInfo> getConnectionList(String user) {
        return connectionInfoRepository.findByUser(user);
    }

    public void refreshAllConnections(String engine) {
        try {
            if (!engineService.isReady(engine)) return;
            connectionInfoRepository.findAll().forEach(connectionInfo ->
                    connectionStatus.get(engine)
                            .put(connectionInfo.getId(), testConnection(connectionInfo, engine))
            );
            log.info("All connection have refreshed!");
        } catch (Exception e) {
            log.error("Error when refresh connection status!", e);
        }
    }

    public void refreshUserConnections(String user) {
        try {
            String engine = engineService.getExecutionEngine();
            connectionInfoRepository.findByUser(user).forEach(connectionInfo ->
                    connectionStatus.get(engine).put(connectionInfo.getId(), testConnection(connectionInfo, engine))
            );
            log.info("User:[" + user + "] connection have refreshed!");
        } catch (Exception e) {
            log.error("Error when refresh connection for user: [" + user + "]", e);
        }
    }

    public Map<Integer, Boolean> getConnectionStatus(List<ConnectionInfo> connectionInfos, Boolean refresh) {
        String engine = engineService.getExecutionEngine();

        Map<Integer, Boolean> statusMap = new HashMap<>();
        Map<Integer, Boolean> cachedStatusMap = connectionStatus.get(engine);

        connectionInfos.forEach(connectionInfo -> {
                    Integer connectionId = connectionInfo.getId();
                    if (refresh) {
                        cachedStatusMap.put(connectionId, testConnection(connectionInfo,
                                engineService.getExecutionEngine()));
                    } else {
                        cachedStatusMap.computeIfAbsent(connectionId, k -> testConnection(connectionInfo,
                                engineService.getExecutionEngine()));
                    }
                    statusMap.put(connectionId, cachedStatusMap.get(connectionId));
                }
        );
        return statusMap;
    }

    @PreDestroy
    public void shutdownConnectionStatusRefresher() {
        log.info("Shutdown ConnectionStatusRefresher");
        this.executor.shutdownNow();
    }
}
