package io.kyligence.notebook.console.bean.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemConfigDTO {

    @JsonProperty("timeout")
    private String timeout;

    @JsonProperty("engine")
    private String engine;

    public static SystemConfigDTO valueOf(SystemConfig systemConfig) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        systemConfigDTO.timeout = EntityUtils.toStr(systemConfig.getTimeout());
        systemConfigDTO.engine = EntityUtils.toStr(systemConfig.getEngine());

        return systemConfigDTO;
    }

}
