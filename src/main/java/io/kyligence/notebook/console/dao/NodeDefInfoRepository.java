package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.NodeDefInfo;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NodeDefInfoRepository extends JpaRepository<NodeDefInfo, Integer> {

    @Query(value = "select * from node_def_info where node_type = ?1", nativeQuery = true)
    List<NodeDefInfo> findByNodeType(String nodeType);

    @Query(value = "select * from node_def_info  where node_type = ?1 and name = ?2", nativeQuery = true)
    List<NodeDefInfo> findNodeByTypeAndName(String nodeType, String nodeName);
}
