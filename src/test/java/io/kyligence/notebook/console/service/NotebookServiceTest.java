package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherTestBase;
import io.kyligence.notebook.console.bean.entity.NotebookCommit;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.dao.NotebookCommitRepository;
import io.kyligence.notebook.console.dao.NotebookRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class NotebookServiceTest extends NotebookLauncherTestBase {
    @Autowired
    private NotebookService nbService;

    @Autowired
    private NotebookRepository nbRepository;

    @Autowired
    private NotebookCommitRepository nbCommitRepository;

    @Test
    public void testCreateNotebook() {
        String testNotebookName = "TEST_CREATE_NOTEBOOK";
        NotebookInfo nb = nbService.create(DEFAULT_ADMIN_USER, testNotebookName, null);
        Assert.assertEquals(testNotebookName, nb.getName());
        NotebookInfo saved = nbRepository.findById(nb.getId()).orElse(null);
        Assert.assertNotNull(saved);
    }

    @Test
    public void testCommitNotebook() {
        NotebookCommit commit = nbService.commit(DEFAULT_ADMIN_USER, Integer.valueOf(defaultMockNotebookId));
        Assert.assertEquals(defaultMockNotebookId, commit.getNotebookId().toString());

        List<NotebookCommit> saved = nbCommitRepository.findByCommit(Integer.valueOf(defaultMockNotebookId), commit.getCommitId());

        Assert.assertEquals(1, saved.size());
    }
}
