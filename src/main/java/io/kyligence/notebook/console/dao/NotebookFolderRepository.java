package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.NotebookFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotebookFolderRepository extends JpaRepository<NotebookFolder, Integer> {

    @Query(value = "select * from notebook_folder where user = ?1", nativeQuery = true)
    List<NotebookFolder> find(String user);

    @Query(value = "select * from notebook_folder where user = ?1 and name = ?2", nativeQuery = true)
    List<NotebookFolder> findByName(String user, String name);

    @Query(value = "select * from notebook_folder where user = ?1 and absolute_path = ?2", nativeQuery = true)
    List<NotebookFolder> findByAbsolutePath(String user, String absolutePath);
}
