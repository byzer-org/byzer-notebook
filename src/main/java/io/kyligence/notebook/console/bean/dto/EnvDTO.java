package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnvDTO {
    @JsonProperty("is_trial")
    private Boolean isTrial;

    @JsonProperty("file_size_limit_kb")
    private Double fileSizeLimitKB;

    public static EnvDTO valueOf(Boolean isTrial, Double userFileSizeLimit) {
        EnvDTO dto = new EnvDTO();
        dto.setIsTrial(isTrial);
        dto.setFileSizeLimitKB(userFileSizeLimit);
        return dto;
    }
}
