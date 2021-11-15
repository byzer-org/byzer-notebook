package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.RegisterET;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ETNodeDTO {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("category")
    private String category;

    @JsonProperty("description")
    private String description;

    @JsonProperty("et_usage")
    private String etUsage;

    @JsonProperty("usage")
    private String usageTemplate;

    @JsonProperty("enable")
    private Boolean enable;

    public static ETNodeDTO valueOf(RegisterET et) {
        ETNodeDTO ETNodeDTO = new ETNodeDTO();
        ETNodeDTO.setId(et.getId());
        ETNodeDTO.setName(et.getName());
        ETNodeDTO.setEtUsage(et.getEtUsage());
        ETNodeDTO.setUsageTemplate(et.getEtUsage());
        ETNodeDTO.setCaption(et.getCaption());
        ETNodeDTO.setEnable(et.getEnable());
        ETNodeDTO.setCategory(et.getCategory());
        ETNodeDTO.setDescription(et.getDescription());
        return ETNodeDTO;
    }
}
