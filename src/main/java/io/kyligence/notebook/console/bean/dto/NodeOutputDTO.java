package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class NodeOutputDTO {
    @JsonProperty("output")
    private List<String> output;

    public static NodeOutputDTO valueOf(Set<String> outputSet){
        NodeOutputDTO nodeOutputDTO = new NodeOutputDTO();
        nodeOutputDTO.setOutput(Lists.newArrayList(outputSet.iterator()));
        return nodeOutputDTO;
    }
}
