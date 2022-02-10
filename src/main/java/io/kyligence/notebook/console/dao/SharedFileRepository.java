package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.SharedFileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SharedFileRepository extends JpaRepository<SharedFileInfo, Integer> {
    @Query(value = "select * from shared_file where owner = ?1 order by id", nativeQuery = true)
    List<SharedFileInfo> findByOwner(String owner);

    @Query(value = "select * from shared_file where entity_id = ?1 and entity_type = ?2", nativeQuery = true)
    List<SharedFileInfo> findByEntity(Integer entityId, String entityType);

    @Query(value = "select * from shared_file where entity_id = ?2 and entity_type = ?3 and owner = ?1", nativeQuery = true)
    List<SharedFileInfo> findByEntity(String owner, Integer entityId, String entityType);

    @Query(value = "select * from shared_file where entity_id = ?2 and entity_type = ?3 and owner = ?1 and commit_id = ?4", nativeQuery = true)
    List<SharedFileInfo> findByCommit(String owner, Integer entityId, String entityType, String commitId);

    @Modifying
    @Transactional
    @Query(value = "delete from shared_file where entity_id = ?2 and entity_type = ?3 and owner = ?1", nativeQuery = true)
    void deleteByEntity(String owner, Integer entityId, String entityType);

    @Modifying
    @Transactional
    @Query(value = "update shared_file set commit_id = ?4 where entity_id = ?2 and entity_type = ?3 and owner = ?1", nativeQuery = true)
    void updateCommit(String owner, Integer entityId, String entityType, String commitId);

}
