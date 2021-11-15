package io.kyligence.notebook.console.bean.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.NodeDefInfo;
import io.kyligence.notebook.console.bean.entity.ParamDefInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class NodeDefDTO {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("node_type")
    private String nodeType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("params")
    private List<ParamDefDTO> params;

    public static NodeDefDTO valueOf(NodeDefInfo defInfo) {
        NodeDefDTO defDTO = new NodeDefDTO();
        defDTO.setId(defInfo.getId());
        defDTO.setName(defInfo.getName());
        defDTO.setNodeType(defInfo.getNodeType());
        defDTO.setDescription(defInfo.getDescription());
        return defDTO;
    }

    public static NodeDefDTO valueOf(NodeDefInfo defInfo, List<ParamDefInfo> paramDefInfoList) {
        NodeDefDTO dto = valueOf(defInfo);
        Map<Integer, List<ParamDefDTO>> bondMap = new HashMap<>();
        List<ParamDefDTO> paramDefDTOList = Lists.newArrayList();
        paramDefInfoList.forEach(paramDefInfo -> {
            Integer bindTo = paramDefInfo.getBind();
            if (bindTo != null){
                bondMap.putIfAbsent(bindTo, Lists.newArrayList());
                bondMap.get(bindTo).add(ParamDefDTO.valueOf(paramDefInfo));
            } else {
                paramDefDTOList.add(ParamDefDTO.valueOf(paramDefInfo));
            }
        });
        paramDefDTOList.forEach(paramDefDTO -> ParamDefDTO.bindParam(paramDefDTO, bondMap));
        dto.setParams(paramDefDTOList);
        return dto;
    }
}
