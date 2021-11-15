package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.UsageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageTemplateRepository extends JpaRepository<UsageTemplate, Integer> {
    UsageTemplate findByUsage(String usage);
}
