package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {
}
