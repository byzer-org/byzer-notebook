package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JobInfoListDTO {

    @JsonProperty("total_size")
    private String totalSize;

    @JsonProperty("list")
    private List<JobInfoDTO> list;

    public static JobInfoListDTO valueOf(Long count, List<JobInfoDTO> list) {
        JobInfoListDTO jobInfoListDTO = new JobInfoListDTO();
        jobInfoListDTO.totalSize = EntityUtils.toStr(count);
        jobInfoListDTO.list = list;
        return jobInfoListDTO;
    }

}
