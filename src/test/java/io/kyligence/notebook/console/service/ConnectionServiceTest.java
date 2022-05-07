package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.dto.ConnectionDTO;
import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import io.kyligence.notebook.console.dao.ConnectionInfoRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ConnectionServiceTest extends NotebookLauncherBaseTest {
    private static final String MOCK_CONNECTION_CONTENT = "[{}]";

    private static ConnectionInfo MOCK_CONNECTION = null;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private ConnectionInfoRepository connectionRepository;

    @Override
    @PostConstruct
    public void mock() {
        if (Objects.isNull(MOCK_CONNECTION)) {
            ConnectionInfo mockConnection = new ConnectionInfo();
            mockConnection.setId(12);
            mockConnection.setName(DEFAULT_ADMIN_USER + "-" + "mockConnectionForAdmin");
            mockConnection.setDriver("com.mysql.jdbc.Driver");
            mockConnection.setUser(DEFAULT_ADMIN_USER);
            mockConnection.setUrl("jdbc:mysql://localhost:3306/notebook");
            mockConnection.setUserName("root");
            mockConnection.setPassword("5de2cf714845fa47b8904e3e46e23e8f");
            mockConnection.setParameter("[{}]");
            MOCK_CONNECTION = connectionRepository.save(mockConnection);
        }
    }

    @Test
    public void testFindById() {
        Assert.assertNull(connectionService.findById(0));
        ConnectionInfo mockConnection = connectionService.findById(MOCK_CONNECTION.getId());
        Assert.assertEquals(DEFAULT_ADMIN_USER + "-" + "mockConnectionForAdmin", mockConnection.getName());
    }

    @Test
    public void testTestConnection() {
        clean("/run/script", "POST", client);
        client.reset();
        client.when(request().withPath("/run/script").withMethod("POST")
        ).respond(response().withBody(MOCK_CONNECTION_CONTENT));
        Assert.assertTrue(connectionService.testConnection(MOCK_CONNECTION, "default"));
        Assert.assertFalse(connectionService.testConnection(MOCK_CONNECTION, "backup"));
        Assert.assertTrue(connectionService.testConnection(ConnectionDTO.valueOf(MOCK_CONNECTION)));

        MOCK_CONNECTION.setParameter("[{\"name\":\"probeSQL\",\"value\": \"select 1 from testTB\"}]");
        Assert.assertTrue(connectionService.testConnection(MOCK_CONNECTION, "default"));
        Assert.assertFalse(connectionService.testConnection(MOCK_CONNECTION, "backup"));
        Assert.assertTrue(connectionService.testConnection(ConnectionDTO.valueOf(MOCK_CONNECTION)));
    }

    @Test
    public void testRenderSQL() {
        String expectSQL = "connect jdbc where\n" +
                "url=\"jdbc:mysql://localhost:3306/notebook\"\n" +
                "and driver=\"com.mysql.jdbc.Driver\"\n" +
                "and user=\"root\"\n" +
                "and password=\"root\"\n" +
                "as admin-mockConnectionForAdmin;";
        Assert.assertEquals(expectSQL, connectionService.renderConnectionSQL(MOCK_CONNECTION));
    }
}
