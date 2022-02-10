package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.NodeCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NodeCommitRepository extends JpaRepository<NodeCommit, Integer> {

    @Query(value = "select * from node_commit where workflow_id = ?1 and commit_id = ?2", nativeQuery = true)
    List<NodeCommit> findByCommit(Integer workflowId, String commit_id);

    @Modifying
    @Transactional
    @Query(value = "delete from node_commit where workflow_id = ?1", nativeQuery = true)
    void deleteByWorkflow(Integer workflowId);

}
