package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.CellInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CellInfoRepository extends JpaRepository<CellInfo, Integer> {

    @Query(value = "select * from cell_info where notebook_id = ?1", nativeQuery = true)
    List<CellInfo> findByNotebook(Integer notebookId);

    @Modifying
    @Transactional
    @Query(value = "delete from cell_info where notebook_id = ?1", nativeQuery = true)
    void deleteByNotebook(Integer notebookId);

    @Modifying
    @Transactional
    @Query(value = "delete from cell_info where id = ?1 and notebook_id = ?2", nativeQuery = true)
    void delete(Integer id, Integer notebookId);

    @Modifying
    @Transactional
    @Query(value = "update cell_info set content = ?3 where id = ?1 and notebook_id = ?2", nativeQuery = true)
    void updateCellContent(Integer id, Integer notebookId, String content);

    @Modifying
    @Transactional
    @Query(value = "update cell_info set last_job_id = ?2 where id = ?1", nativeQuery = true)
    void updateCellJobId(Integer id, String job_id);
}
