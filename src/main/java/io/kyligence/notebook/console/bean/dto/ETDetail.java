package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ETDetail {

    @JsonProperty("usage")
    private String usage;

    @JsonProperty("name")
    private String name;

    @JsonProperty("params")
    private List<ETParamDTO> params;

    public static ETDetail valueOf(String name, String usage, List<ETParamDTO> params) {
        ETDetail etDetail = new ETDetail();
        etDetail.setName(name);
        etDetail.setUsage(usage);
        etDetail.setParams(params);
        return etDetail;
    }

}
