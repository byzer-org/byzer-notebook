package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.ConnectionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConnectionInfoRepository extends JpaRepository<ConnectionInfo, Integer> {
    @Query(value = "select * from connection_info where user = ?1", nativeQuery = true)
    List<ConnectionInfo> findByUser(String user);

    @Query(value = "select * from connection_info where name = ?1", nativeQuery = true)
    List<ConnectionInfo> findByName(String name);

    @Query(value = "select * from connection_info where user = ?1 and name = ?2", nativeQuery = true)
    List<ConnectionInfo> findByUserAndName(String user, String name);

    @Modifying
    @Transactional
    @Query(value = "delete from connection_info where id = ?1", nativeQuery = true)
    void deleteById(Integer connectionID);

    @Modifying
    @Transactional
    @Query(value = "update connection_info set name = ?2 where id = ?1", nativeQuery = true)
    void updateConnectionName(Integer connectionID, String name);
}
