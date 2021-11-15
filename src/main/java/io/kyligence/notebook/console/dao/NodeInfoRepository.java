package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.NodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NodeInfoRepository extends JpaRepository<NodeInfo, Integer> {

    @Query(value = "select * from workflow_node where workflow_id = ?1", nativeQuery = true)
    List<NodeInfo> findByWorkflow(Integer workflowId);

    @Query(value = "select * from workflow_node where workflow_id = ?1 and type = ?2", nativeQuery = true)
    List<NodeInfo> findByWorkflowAndNodeType(Integer workflowId, String nodeType);

    @Modifying
    @Transactional
    @Query(value = "delete from workflow_node where workflow_id = ?1", nativeQuery = true)
    void deleteByWorkflow(Integer workflowId);

    @Modifying
    @Transactional
    @Query(value = "delete from workflow_node where id = ?1 and workflow_id = ?2", nativeQuery = true)
    void delete(Integer id, Integer workflowId);

    @Modifying
    @Transactional
    @Query(value = "update workflow_node set content = ?3, input = ?4, output = ?5 where id = ?1 and workflow_id = ?2", nativeQuery = true)
    void updateNode(Integer id, Integer workflowId, String content, String input, String output);

    @Modifying
    @Transactional
    @Query(value = "update workflow_node set position = ?2 where id = ?1", nativeQuery = true)
    void updateNodePosition(Integer id, String position);

}
