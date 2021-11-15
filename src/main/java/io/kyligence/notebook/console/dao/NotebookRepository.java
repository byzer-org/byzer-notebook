package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotebookRepository extends JpaRepository<NotebookInfo, Integer> {

    @Query(value = "select * from notebook_info where user = ?1 and name = ?2", nativeQuery = true)
    List<NotebookInfo> find(String user, String name);

    @Query(value = "select * from notebook_info where user = ?1", nativeQuery = true)
    List<NotebookInfo> find(String user);

    @Query(value = "select * from notebook_info where user = ?1 and name = ?2 and folder_id = ?3", nativeQuery = true)
    List<NotebookInfo> find(String user, String name, Integer folderId);

    @Query(value = "select * from notebook_info where user = ?1 and type = ?2 order by id", nativeQuery = true)
    List<NotebookInfo> findByType(String user, String type);

    @Query(value = "select count(id) from notebook_info where user = ?1", nativeQuery = true)
    Integer getUserNotebookCount(String user);

}
