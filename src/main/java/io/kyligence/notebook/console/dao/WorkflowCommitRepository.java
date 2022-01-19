package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.WorkflowCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WorkflowCommitRepository extends JpaRepository<WorkflowCommit, Integer> {

    @Query(value = "select * from workflow_commit where workflow_id = ?1", nativeQuery = true)
    List<WorkflowCommit> listCommit(Integer workflowId);

    @Query(value = "select * from workflow_commit where workflow_id = ?1 and commit_id = ?2", nativeQuery = true)
    List<WorkflowCommit> findByCommit(Integer workflowId, String commitId);

    @Modifying
    @Transactional
    @Query(value = "delete from workflow_commit where workflow_id = ?1", nativeQuery = true)
    void deleteByWorkflow(Integer workflowId);
}
