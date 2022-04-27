package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.bean.entity.CellInfo;
import io.kyligence.notebook.console.bean.entity.NotebookCommit;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.dao.NotebookCommitRepository;
import io.kyligence.notebook.console.dao.NotebookRepository;
import io.kyligence.notebook.console.tools.ImportResponseDTO;
import io.kyligence.notebook.console.util.JacksonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class NotebookServiceTest extends NotebookLauncherBaseTest {

    @Autowired
    private NotebookService nbService;

    @Autowired
    private NotebookRepository nbRepository;

    @Autowired
    private NotebookCommitRepository nbCommitRepository;

    private Integer importMockNotebook() throws Exception {
        ImportResponseDTO dto = JacksonUtils.readJson(
                importNotebook(MOCK_NOTEBOOK)
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                ImportResponseDTO.class);
        return Integer.valueOf(Objects.requireNonNull(dto).getData().get(0).getId());
    }

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

    @Test
    public void testListCommits() {
        Assert.assertNotNull(nbService.listCommits(DEFAULT_ADMIN_USER, Integer.valueOf(defaultMockNotebookId)));
    }

    @Test
    public void testDeleteCell() throws Exception {
        Integer notebookId = importMockNotebook();
        List<CellInfo> cellInfoList = nbService.getCellInfos(notebookId);
        nbService.deleteCell(cellInfoList.get(0));
        List<CellInfo> deletedCellInfoList = nbService.getCellInfos(notebookId);
        Assert.assertEquals(cellInfoList.size() - 1, deletedCellInfoList.size());
    }

    @Test
    public void testSaveCell() {
        long currentTimeStamp = System.currentTimeMillis();
        String content = "select 1 as a as output;";
        CellInfo cellInfo = new CellInfo();
        cellInfo.setNotebookId(Integer.valueOf(defaultMockNotebookId));
        cellInfo.setContent(content);
        cellInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        nbService.save(cellInfo);
        CellInfo savedCellInfo = nbService.getCellInfo(cellInfo.getId());
        Assert.assertNotNull(savedCellInfo);
        Assert.assertEquals(Integer.valueOf(defaultMockNotebookId), savedCellInfo.getNotebookId());
        Assert.assertEquals(content, savedCellInfo.getContent());
    }

    @Test
    public void testGetCellInfosByNotebookId() throws Exception {
        Integer notebookId = importMockNotebook();
        List<CellInfo> cellInfoList = nbService.getCellInfos(notebookId);
        Assert.assertEquals(2, cellInfoList.size());
    }

    @Test
    public void testGetCellInfosByCellInfoList() throws Exception {
        Integer notebookId = importMockNotebook();
        List<CellInfo> cellInfoList = nbService.getCellInfos(notebookId);
        String cellIds = "";
        for (CellInfo cell: cellInfoList) {
            cellIds += cell.getId().toString() + ",";
        }
        cellIds += "]";
        List<CellInfo> cellInfosByCellIds = nbService.getCellInfos(cellIds);
        Assert.assertEquals(cellInfoList, cellInfosByCellIds);
    }

    @Test
    public void testGetCellInfo() {
        CellInfo cellInfo = nbService.getCellInfos(Integer.valueOf(defaultMockNotebookId)).get(0);
        Assert.assertEquals(cellInfo, nbService.getCellInfo(cellInfo.getId()));
        Assert.assertNull(nbService.getCellInfo(0));
    }

    @Test
    public void testIsNotebookExist() {
        NotebookInfo nbInfo = nbService.findById(Integer.valueOf(defaultMockNotebookId));
        Assert.assertTrue(nbService.isNotebookExist(DEFAULT_ADMIN_USER, nbInfo.getName(), nbInfo.getFolderId()));
        Assert.assertFalse(nbService.isNotebookExist(DEFAULT_ADMIN_USER, "not exist nb", null));
    }

    @Test
    public void testUpdateCellContent() {
        String newContent = "select \"isMock\" as mockData as output;";
        CellInfo cellInfo = nbService.getCellInfos(Integer.valueOf(defaultMockNotebookId)).get(0);
        cellInfo.setContent(newContent);
        nbService.updateCellContent(cellInfo);
        CellInfo newCellInfo = nbService.getCellInfo(cellInfo.getId());
        Assert.assertEquals(newContent, newCellInfo.getContent());
    }

    @Test
    public void testUpdateCellJobId() {
        String newJobId = "7aaa242e-88fe-4500-a379-38e911e391b6";
        CellInfo cellInfo = nbService.getCellInfos(Integer.valueOf(defaultMockNotebookId)).get(0);
        cellInfo.setLastJobId(newJobId);
        nbService.updateCellJobId(cellInfo);
        CellInfo newCellInfo = nbService.getCellInfo(cellInfo.getId());
        Assert.assertEquals(newJobId, newCellInfo.getLastJobId());
    }

    @Test
    public void testSaveNotebook() {
        String newName = "new name";
        long currentTimeStamp = System.currentTimeMillis();
        NotebookInfo nbInfo = nbService.findById(Integer.valueOf(defaultMockNotebookId));
        nbInfo.setName(newName);
        nbInfo.setUpdateTime(new Timestamp(currentTimeStamp));
        nbService.save(nbInfo);
        NotebookInfo newNotebookInfo = nbService.findById(Integer.valueOf(defaultMockNotebookId));
        Assert.assertNotNull(newNotebookInfo);
        Assert.assertEquals(newName, newNotebookInfo.getName());
        Assert.assertEquals(currentTimeStamp, newNotebookInfo.getUpdateTime().getTime());
    }

    @Test
    public void testDeleteNotebook() {
        String testNotebookName = "TEST_DELETE_NOTEBOOK";
        NotebookInfo nb = nbService.create(DEFAULT_ADMIN_USER, testNotebookName, null);
        nbService.delete(nb.getId());
        Assert.assertNull(nbService.findById(nb.getId()));
    }
}
