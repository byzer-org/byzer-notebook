package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class EngineServiceTest extends NotebookLauncherBaseTest {

    private final String testSQL = "!show version";

    @Autowired
    private EngineService engineService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    @PostConstruct
    public void mock() {

    }

    @Override
    public String getCollectionName() {
        return "engine";
    }

    @Test
    public void testRunScript() {
        String resp = engineService.runScript(new EngineService.RunScriptParams().withSql(testSQL).withAsync("false"));
        Assert.assertNotNull(resp);
    }

    @Test
    public void testRunAnalyze() {
        String resp = engineService.runAnalyze(new EngineService.RunScriptParams()
                .withSql("load Everything.`` as Table;").withAsync("false"));
        Assert.assertNotNull(resp);
    }

    @Test
    public void testRunAutoSuggest() {
        String resp = engineService.runAutoSuggest(new EngineService.RunScriptParams().withSql("s").withAsync("false"));
        Assert.assertNotNull(resp);
    }

}
