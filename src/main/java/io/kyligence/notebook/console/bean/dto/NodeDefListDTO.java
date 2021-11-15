package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.NodeDefInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class NodeDefListDTO {
    @JsonProperty("def_list")
    private List<NodeDefDTO> defList;

    @JsonProperty("size")
    private Integer size;

    public static NodeDefListDTO valueOf(List<NodeDefInfo> defInfoList){
        NodeDefListDTO dto = new NodeDefListDTO();
        dto.setSize(defInfoList.size());
        dto.setDefList(defInfoList.stream().map(NodeDefDTO::valueOf).collect(Collectors.toList()));
        return dto;
    }
}
