package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.ModelInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ModelInfoListDTO {
    @JsonProperty("total_size")
    private Integer totalSize;

    @JsonProperty("models")
    private List<ModelInfoDTO> models;

    public static ModelInfoListDTO valueOf(List<ModelInfo> models) {
        ModelInfoListDTO dto = new ModelInfoListDTO();
        dto.setModels(models.stream().map(ModelInfoDTO::valueOf).collect(Collectors.toList()));
        dto.setTotalSize(models.size());
        return dto;
    }
}
