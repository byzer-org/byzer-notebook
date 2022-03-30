package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherTestBase;
import io.kyligence.notebook.console.bean.dto.NodeInfoDTO;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.bean.entity.WorkflowCommit;
import io.kyligence.notebook.console.bean.entity.WorkflowInfo;
import io.kyligence.notebook.console.dao.NodeCommitRepository;
import io.kyligence.notebook.console.dao.NodeInfoRepository;
import io.kyligence.notebook.console.dao.WorkflowCommitRepository;
import io.kyligence.notebook.console.dao.WorkflowRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.NodeUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

public class WorkflowServiceTest extends NotebookLauncherTestBase {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private NodeInfoRepository nodeRepository;

    @Autowired
    private NodeCommitRepository ncRepository;

    @Autowired
    private WorkflowCommitRepository wcRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Integer mockWorkflowId;

    private final String mockWorkflowName = "mock-workflow";

    private final String mockWorkflowCellContent = "select hello as world from tA as tB;";

    @Override
    @PostConstruct
    public void mock() {
        WorkflowInfo workflowInfo = new WorkflowInfo();
        workflowInfo.setUser(DEFAULT_ADMIN_USER);
        workflowInfo.setName(mockWorkflowName);
        workflowInfo = workflowRepository.save(workflowInfo);
        mockWorkflowId = workflowInfo.getId();

        // mock node
        NodeInfoDTO.NodeContent nodeContent = new NodeInfoDTO.NodeContent();
        nodeContent.setSql(mockWorkflowCellContent);
        NodeInfoDTO.NodePosition position = new NodeInfoDTO.NodePosition();
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setContent(JacksonUtils.writeJson(nodeContent));
        nodeInfo.setUser(DEFAULT_ADMIN_USER);
        nodeInfo.setInput("[\"tA\"]");
        nodeInfo.setOutput("[\"tB\"]");
        nodeInfo.setWorkflowId(mockWorkflowId);
        nodeInfo.setType(NodeUtils.NodeType.SELECT);
        nodeInfo.setPosition(JacksonUtils.writeJson(position));
        nodeRepository.save(nodeInfo);

    }

    @Test
    public void testFindById() {
        WorkflowInfo workflowInfo = workflowService.findById(mockWorkflowId);
        Assert.assertNotNull(workflowInfo);
        Assert.assertEquals(mockWorkflowId, workflowInfo.getId());
        Assert.assertEquals(mockWorkflowName, workflowInfo.getName());
    }

    @Test
    public void testFind() {
        WorkflowInfo workflowInfo = workflowService.find(DEFAULT_ADMIN_USER, mockWorkflowName, null);
        Assert.assertNotNull(workflowInfo);
        Assert.assertEquals(mockWorkflowName, workflowInfo.getName());

        WorkflowInfo notExist = workflowService.find("not-exist-user", mockWorkflowName, null);
        Assert.assertNull(notExist);

        notExist = workflowService.find(DEFAULT_ADMIN_USER, "not-exist-workflow", null);
        Assert.assertNull(notExist);

        List<WorkflowInfo> workflowInfoList = workflowService.find(DEFAULT_ADMIN_USER);
        Assert.assertNotEquals(0, workflowInfoList.size());

    }

    @Test
    public void testCreate() {
        String testWorkflowName = "test-create-workflow";
        WorkflowInfo workflowInfo = workflowService.create(DEFAULT_ADMIN_USER, testWorkflowName, null);
        Assert.assertEquals(testWorkflowName, workflowInfo.getName());
    }

    @Test
    public void testCommit() {
        WorkflowCommit wCommit = workflowService.commit(DEFAULT_ADMIN_USER, mockWorkflowId);
        Assert.assertNotNull(wcRepository.findByCommit(mockWorkflowId, wCommit.getCommitId()));

        Assert.assertNotEquals(0, ncRepository.findByCommit(mockWorkflowId, wCommit.getCommitId()).size());
    }

    @Test
    public void testRename() {
        String userForTest = "testUserForRename";
        WorkflowInfo workflowInfo = workflowService.create(userForTest, "testWfForRename", null);
        String newName = "testRenamed";

        WorkflowInfo renamed = workflowService.rename(workflowInfo.getId(), userForTest, newName);
        Assert.assertEquals(newName, renamed.getName());

        thrown.expect(ByzerException.class);
        thrown.expectMessage("Workflow Not Available");
        workflowService.rename(mockWorkflowId, userForTest, newName);
        thrown.expectMessage("Workflow Not Exist");
        workflowService.rename(0, userForTest, newName);

    }

    @Test
    public void testCreateNode() {
        NodeInfoDTO.NodeContent c = new NodeInfoDTO.NodeContent();
        c.setSql("select * from tB as tC");
        NodeInfo nodeInfo = workflowService.createNode(mockWorkflowId, DEFAULT_ADMIN_USER, NodeUtils.NodeType.SELECT,
                c, new NodeInfoDTO.NodePosition());
        Assert.assertNotNull(nodeInfo);
        Assert.assertEquals("[\"tB\"]", nodeInfo.getInput().replace(" ", ""));
        Assert.assertEquals("[\"tC\"]", nodeInfo.getOutput().replace(" ", ""));

        thrown.expect(ByzerException.class);
        thrown.expectMessage("Node Output Already Exist");
        workflowService.createNode(mockWorkflowId, DEFAULT_ADMIN_USER, NodeUtils.NodeType.SELECT,
                c, new NodeInfoDTO.NodePosition());

        c.setSql("select * from tB as tA");
        thrown.expectMessage("Workflow Has Cycle");
        workflowService.createNode(mockWorkflowId, DEFAULT_ADMIN_USER, NodeUtils.NodeType.SELECT,
                c, new NodeInfoDTO.NodePosition());

    }

    @Test
    public void testUpdateNode() {
        NodeInfoDTO.NodeContent c = new NodeInfoDTO.NodeContent();
        c.setSql("select * from tX as tZ");
        NodeInfo nodeInfo = workflowService.createNode(mockWorkflowId, DEFAULT_ADMIN_USER, NodeUtils.NodeType.SELECT,
                c, new NodeInfoDTO.NodePosition());

        String sql = "select * from tX as tY";
        c.setSql(sql);
        NodeInfo updated = workflowService.updateNode(mockWorkflowId, nodeInfo.getId(), DEFAULT_ADMIN_USER,
                NodeUtils.NodeType.SELECT, c);
        String updatedSQL = Objects.requireNonNull(JacksonUtils.readJson(updated.getContent(),
                NodeInfoDTO.NodeContent.class)).getSql();
        Assert.assertEquals(updatedSQL, sql);

        thrown.expect(ByzerException.class);

        thrown.expectMessage("Node Not Exist");
        workflowService.updateNode(mockWorkflowId, 0, DEFAULT_ADMIN_USER,
                NodeUtils.NodeType.SELECT, c);

        thrown.expectMessage("Can't Change Type of Exist Node");
        workflowService.updateNode(mockWorkflowId, nodeInfo.getId(), DEFAULT_ADMIN_USER,
                NodeUtils.NodeType.ET, c);

        thrown.expectMessage("Node Output Already Exist");
        c.setSql("select * from tX as tB");
        workflowService.updateNode(mockWorkflowId, nodeInfo.getId(), DEFAULT_ADMIN_USER,
                NodeUtils.NodeType.SELECT, c);

        thrown.expectMessage("Workflow Has Cycle");
        c.setSql("select * from tB as tA");
        workflowService.updateNode(mockWorkflowId, nodeInfo.getId(), DEFAULT_ADMIN_USER,
                NodeUtils.NodeType.SELECT, c);

    }

    @Test
    public void testDeleteNode() {
        NodeInfoDTO.NodeContent c = new NodeInfoDTO.NodeContent();
        c.setSql("select * from tL as tM");
        NodeInfo nodeInfo = workflowService.createNode(mockWorkflowId, DEFAULT_ADMIN_USER, NodeUtils.NodeType.SELECT,
                c, new NodeInfoDTO.NodePosition());
        workflowService.deleteNode(mockWorkflowId, nodeInfo.getId(), DEFAULT_ADMIN_USER);
        Assert.assertNull(workflowService.findNodeById(nodeInfo.getId()));
    }

    @Test
    public void testUpdateNodePosition() {
        NodeInfoDTO.NodeContent c = new NodeInfoDTO.NodeContent();
        NodeInfoDTO.NodePosition p = new NodeInfoDTO.NodePosition();
        p.setX("100");
        p.setY("100");
        c.setSql("select * from tP as tQ");
        NodeInfo nodeInfo = workflowService.createNode(mockWorkflowId, DEFAULT_ADMIN_USER, NodeUtils.NodeType.SELECT,
                c, p);

        p.setY("200");

        NodeInfo updated = workflowService.updateNodePosition(mockWorkflowId, nodeInfo.getId(),
                DEFAULT_ADMIN_USER, p);

        String newY = Objects.requireNonNull(JacksonUtils.readJson(updated.getPosition(),
                NodeInfoDTO.NodePosition.class)).getY();
        Assert.assertEquals("200", newY);

    }

    @Test
    public void testListOutput() {
        Assert.assertTrue(workflowService.listOutput(mockWorkflowId).contains("tB"));
    }

    @Test
    public void testWorkflowToNotebook() {
        String user = "userForConvert";
        String name = "workflowForConvert";
        WorkflowInfo workflowInfo = workflowService.create(user, name, null);
        NodeInfoDTO.NodeContent nodeContent = new NodeInfoDTO.NodeContent();
        nodeContent.setSql(mockWorkflowCellContent);
        workflowService.createNode(workflowInfo.getId(), user, NodeUtils.NodeType.SELECT,
                nodeContent, new NodeInfoDTO.NodePosition());

        NotebookInfo nb = workflowService.workflowToNotebook(workflowInfo.getId(), user, name);
        List<Integer> cellIds = JacksonUtils.readJsonArray(nb.getCellList(), Integer.class);
        Assert.assertNotNull(cellIds);
        Assert.assertEquals(1, cellIds.size());
        Assert.assertEquals(mockWorkflowCellContent, notebookService.getCellInfo(cellIds.get(0)).getContent());

        thrown.expect(ByzerException.class);
        thrown.expectMessage("");
        workflowService.workflowToNotebook(workflowInfo.getId(), user, name);
    }

    @Test
    public void testDelete() {
        WorkflowInfo workflowInfo = workflowService.create("userForDelete", "WorkflowForDelete",
                null);
        workflowService.delete(workflowInfo.getId());

        Assert.assertNull(workflowService.findById(workflowInfo.getId()));
    }
}
