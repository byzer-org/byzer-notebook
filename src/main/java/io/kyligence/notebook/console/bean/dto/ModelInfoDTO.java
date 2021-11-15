package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ModelInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModelInfoDTO {

    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("path")
    private String path;

    @JsonProperty("group_size")
    private Integer groupSize;

    @JsonProperty("workflow_id")
    private Integer workflowId;

    public static ModelInfoDTO valueOf(ModelInfo modelInfo) {
        ModelInfoDTO dto = new ModelInfoDTO();
        dto.setAlgorithm(modelInfo.getAlgorithm());
        dto.setPath(modelInfo.getPath());
        dto.setGroupSize(modelInfo.getGroupSize());
        dto.setWorkflowId(modelInfo.getWorkflowId());
        return dto;
    }
}
