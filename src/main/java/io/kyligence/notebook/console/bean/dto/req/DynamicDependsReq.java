package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.ParamValueMap;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DynamicDependsReq {

    @JsonProperty("dependence_values")
    private List<ParamValueMap> dependenceValues;

    @JsonProperty("scripts")
    private List<ParamSQLMap> scripts;

    @Data
    @NoArgsConstructor
    public static class ParamSQLMap {
        @JsonProperty("name")
        private String name;

        @JsonProperty("sql")
        private String sql;

    }

}
