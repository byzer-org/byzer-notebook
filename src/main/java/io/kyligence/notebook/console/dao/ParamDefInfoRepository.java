package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.bean.entity.ParamDefInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParamDefInfoRepository extends JpaRepository<ParamDefInfo, Integer> {

    @Query(value = "select * from param_def_info where node_def_id = ?1", nativeQuery = true)
    List<ParamDefInfo> findByNodeDef(Integer nodeDefId);

    @Query(value = "select * from param_def_info where node_def_id = ?1 and name = ?2", nativeQuery = true)
    List<ParamDefInfo> findByName(Integer nodeDefId, String paramName);

}
