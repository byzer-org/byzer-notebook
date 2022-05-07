package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.dao.SystemConfigRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static org.mockito.Mockito.when;

public class SystemServiceTest extends NotebookLauncherBaseTest {

    private SystemConfig systemConfig;

    @Autowired
    private SystemService systemService;

    @InjectMocks
    private SystemService mockService;

    @Mock
    private SystemConfigRepository mockRepo;

    @Override
    @PostConstruct
    public void mock() {
        systemConfig = systemService.getConfig(DEFAULT_ADMIN_USER);
    }

    @Test
    public void testGetConfig() {
        Assert.assertEquals(systemConfig.getEngine(), systemService.getConfig("mockConfigUser1").getEngine());
        Assert.assertEquals(systemConfig.getTimeout(), systemService.getConfig("mockConfigUser1").getTimeout());
        Assert.assertEquals(systemConfig.getId(), systemService.getConfig(DEFAULT_ADMIN_USER).getId());
    }

    @Test
    public void testUpdateConfig() {
        SystemConfig config = systemService.getConfig("mockConfigUser2");
        config.setEngine("backup");
        systemService.updateByUser(config);
        config = systemService.getConfig("mockConfigUser2");
        Assert.assertEquals("backup", config.getEngine());
    }

    @Test
    public void testMetaDBReachable () {
        Assert.assertTrue(systemService.isMetaDBReachable());

        when(mockRepo.findAll()).thenThrow(ByzerException.class);

        Assert.assertFalse(mockService.isMetaDBReachable());
    }
}
