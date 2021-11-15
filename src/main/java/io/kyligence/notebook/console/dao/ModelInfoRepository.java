package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.ModelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ModelInfoRepository extends JpaRepository<ModelInfo, Integer> {

    @Query(value = "select * from model_info where workflow_id = ?1", nativeQuery = true)
    List<ModelInfo> findByWorkflowId(Integer workflowId);

    @Query(value = "select * from model_info where user_name = ?1", nativeQuery = true)
    List<ModelInfo> findByUser(String userName);

    @Modifying
    @Transactional
    @Query(value = "delete from model_info where node_id = ?1", nativeQuery = true)
    void deleteByNodeId(Integer nodeId);

    @Modifying
    @Transactional
    @Query(value = "delete from model_info where workflow_id = ?1", nativeQuery = true)
    void deleteByWorkflowId(Integer workflowId);

}