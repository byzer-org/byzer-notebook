package io.kyligence.notebook.console.service;


import io.kyligence.notebook.console.bean.dto.NodeDefDTO;
import io.kyligence.notebook.console.bean.dto.ParamDefDTO;
import io.kyligence.notebook.console.bean.entity.NodeDefInfo;
import io.kyligence.notebook.console.bean.entity.ParamDefInfo;
import io.kyligence.notebook.console.dao.NodeDefInfoRepository;
import io.kyligence.notebook.console.dao.ParamDefInfoRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NodeDefService {

    @Autowired
    private NodeDefInfoRepository nodeDefInfoRepository;

    @Autowired
    private ParamDefInfoRepository paramDefInfoRepository;

    private Map<String, List<ParamDefDTO>> algoParams;

    public List<NodeDefInfo> getNodeDefList(String nodeType) {
        return nodeDefInfoRepository.findByNodeType(nodeType);
    }

    public NodeDefInfo getNode(String nodeType, String nodeName){
        List<NodeDefInfo> nodes = nodeDefInfoRepository.findNodeByTypeAndName(nodeType, nodeName);
        if (nodes.size() < 1){
            throw new ByzerException(ErrorCodeEnum.NODE_DEFINE_NOT_EXIST);
        }
        return nodes.get(0);
    }

    public NodeDefInfo getNodeDefById(Integer id) {
        return nodeDefInfoRepository.findById(id).orElse(null);
    }


    public ParamDefInfo getParamDefByName(Integer nodeDefId, String paramName){
        List<ParamDefInfo> params = paramDefInfoRepository.findByName(nodeDefId, paramName);
        if (params.size() < 1){
            throw new ByzerException(ErrorCodeEnum.PARAM_DEFINE_NOT_EXIST);
        }
        return params.get(0);
    }

    public List<ParamDefInfo> getParamDefByNodeDefId(Integer id) {
        return paramDefInfoRepository.findByNodeDef(id);
    }

    public Map<String, List<ParamDefDTO>> getAlgoParamSettings(){
        if (algoParams == null){
            Map<String, List<ParamDefDTO>> map = new HashMap<>();
            List<NodeDefInfo> nodeDefInfoList = getNodeDefList("train");
            nodeDefInfoList.forEach(defInfo -> {
                List<ParamDefInfo> paramDefInfoList = getParamDefByNodeDefId(defInfo.getId());
                map.put(defInfo.getName(), NodeDefDTO.valueOf(defInfo, paramDefInfoList).getParams());
            });
            algoParams = map;
        }
        return algoParams;
    }
}
