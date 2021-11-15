package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.bean.dto.ConnectionDTO;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.dao.ConnectionInfoRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
import io.kyligence.notebook.console.util.ConnectionUtils;
import io.kyligence.notebook.console.util.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;


@Service
public class ConnectionService {
    @Autowired
    private ConnectionInfoRepository connectionInfoRepository;

    @Autowired
    private EngineService engineService;

    @Autowired
    private CriteriaQueryBuilder criteriaQueryBuilder;

    public ConnectionInfo findById(Integer id) {
        return connectionInfoRepository.findById(id).orElse(null);
    }

    public List<ConnectionInfo> findByUser(String user) {
        return connectionInfoRepository.findByUser(user);
    }

    public boolean testConnection(ConnectionDTO content) {
        if (content.getName() == null) content.setName("UserConnectionTmp");
        String sql = ConnectionUtils.renderSQL(content);
        return checkConnection(sql, content.getName());
    }

    public boolean testConnection(ConnectionInfo info) {
        return checkConnection(renderConnectionSQL(info), info.getName());
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

    private boolean checkConnection(String renderedSQL, String connectionName) {
        EngineService.RunScriptParams runScriptParams = new EngineService.RunScriptParams();

        try {
            engineService.runScript(
                    runScriptParams
                            .withSql(renderedSQL)
                            .withAsync("false")
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

    public Map<Integer, Boolean> getConnectionStatus(List<ConnectionInfo> connectionInfos) {
        Map<Integer, Boolean> statusMap = new HashMap<>();
        connectionInfos.forEach(connectionInfo ->
                statusMap.put(connectionInfo.getId(), testConnection(connectionInfo))
        );
        return statusMap;
    }
}
