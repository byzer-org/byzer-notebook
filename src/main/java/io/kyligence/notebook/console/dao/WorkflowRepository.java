package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.WorkflowInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WorkflowRepository extends JpaRepository<WorkflowInfo, Integer> {

    @Query(value = "select count(id) from workflow_info where user = ?1", nativeQuery = true)
    Integer getUserWorkflowCount(String user);

    @Query(value = "select * from workflow_info where user = ?1 and name = ?2", nativeQuery = true)
    List<WorkflowInfo> find(String user, String name);

    @Query(value = "select * from workflow_info where user = ?1", nativeQuery = true)
    List<WorkflowInfo> find(String user);

    @Query(value = "select * from workflow_info where user = ?1 and name = ?2 and folder_id = ?3", nativeQuery = true)
    List<WorkflowInfo> find(String user, String name, Integer folderId);

    @Modifying
    @Transactional
    @Query(value = "update workflow_info set node_position = ?2 where id = ?1", nativeQuery = true)
    void updateNodePosition(Integer id, String nodePosition);

    @Modifying
    @Transactional
    @Query(value = "update workflow_info set name = ?2 where id = ?1", nativeQuery = true)
    void rename(Integer id, String name);
}
