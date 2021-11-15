package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.ETParamsDef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ETParamsDefRepository extends JpaRepository<ETParamsDef, Integer> {
    List<ETParamsDef> findAllByEtId(Integer etId);
}
