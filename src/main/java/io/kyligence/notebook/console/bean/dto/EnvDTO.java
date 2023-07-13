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

    @JsonProperty("is_scheduler_enabled")
    private Boolean isSchedulerEnabled;

    @JsonProperty("logo")
    private String logo;

    public static EnvDTO valueOf(Boolean isTrial, Double userFileSizeLimit, Boolean isSchedulerEnabled, String logo) {
        EnvDTO dto = new EnvDTO();
        dto.setIsTrial(isTrial);
        dto.setFileSizeLimitKB(userFileSizeLimit);
        dto.setIsSchedulerEnabled(isSchedulerEnabled);
        dto.setLogo(logo);
        return dto;
    }
}
