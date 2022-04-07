package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {

    @Query(value = "select * from system_config where `user` = ?1", nativeQuery = true)
    List<SystemConfig> findByUser(String user);
}
