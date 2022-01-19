package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.CellCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CellCommitRepository extends JpaRepository<CellCommit, Integer> {

    @Query(value = "select * from cell_commit where notebook_id = ?1 and commit_id = ?2", nativeQuery = true)
    List<CellCommit> findByCommit(Integer notebookId, String commitId);

    @Modifying
    @Transactional
    @Query(value = "delete from cell_commit where notebook_id = ?1", nativeQuery = true)
    void deleteByNotebook(Integer notebookId);
}
