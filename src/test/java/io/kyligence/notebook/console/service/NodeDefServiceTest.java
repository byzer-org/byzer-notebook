package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookLauncherBaseTest;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.util.NodeUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class NodeDefServiceTest extends NotebookLauncherBaseTest {

    @Autowired
    private NodeDefService nodeDefService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final Integer LOAD_HDFS_NODE_DEF_ID = 6;

    @Test
    public void testGetNodeDefList() {
        Assert.assertEquals(1, nodeDefService.getNodeDefList(NodeUtils.NodeType.LOAD).size());
        Assert.assertEquals(0, nodeDefService.getNodeDefList(NodeUtils.NodeType.ET).size());
    }

    @Test
    public void testGetNode() {
        Assert.assertNotNull(nodeDefService.getNode(NodeUtils.NodeType.LOAD, "hdfs"));
        thrown.expect(ByzerException.class);
        thrown.expectMessage("Node Define Not Exist");
        nodeDefService.getNode(NodeUtils.NodeType.ET, "not-exist");
    }

    @Test
    public void testGetNodeDefById() {
        Assert.assertEquals("hdfs", nodeDefService.getNodeDefById(LOAD_HDFS_NODE_DEF_ID).getName());

        Assert.assertNull(nodeDefService.getNodeDefById(0));
    }

    @Test
    public void testGetParamDefByName() {
        Assert.assertNotNull(nodeDefService.getParamDefByName(LOAD_HDFS_NODE_DEF_ID, "data_type"));
        thrown.expect(ByzerException.class);
        thrown.expectMessage("Parameter Define Not Exist");
        nodeDefService.getParamDefByName(0, "csv");
    }

    @Test
    public void testGetParamDefByNodeDefId() {
        Assert.assertNotEquals(0, nodeDefService.getParamDefByNodeDefId(6).size());
        Assert.assertEquals(0, nodeDefService.getParamDefByNodeDefId(0).size());

    }

    @Test
    public void testGetAlgoParamSettings() {
        Assert.assertNotEquals(0, nodeDefService.getAlgoParamSettings().size());
    }
}
