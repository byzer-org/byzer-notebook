package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.dao.SystemConfigRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class SystemServiceTest extends NotebookLauncherBaseTest {

    private final NotebookConfig notebookConfig = NotebookConfig.getInstance();

    private SystemConfig systemConfig = new SystemConfig();

    @Autowired
    private SystemService systemService;

    @Autowired
    private SystemConfigRepository scRepository;

    @Override
    @PostConstruct
    public void mock() {
        systemConfig.setTimeout(666);
    }

    @Test
    public void testGetConfig() {
        Assert.assertEquals(scRepository.findAll().get(0), systemService.getConfig());
    }

    @Test
    public void testUpdateConfig() {
        systemService.updateConfig(systemConfig);
        Assert.assertEquals(systemConfig, systemService.getConfig());
    }
}
