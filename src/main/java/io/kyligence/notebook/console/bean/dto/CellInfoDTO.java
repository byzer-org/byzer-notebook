package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.CellCommit;
import io.kyligence.notebook.console.bean.entity.CellInfo;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CellInfoDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("job_id")
    private String jobId;

    public static CellInfoDTO valueOf(CellInfo cellInfo) {
        if (cellInfo == null) {
            return null;
        }

        CellInfoDTO cellInfoDTO = new CellInfoDTO();
        cellInfoDTO.setId(EntityUtils.toStr(cellInfo.getId()));
        cellInfoDTO.setContent(cellInfo.getContent());
        cellInfoDTO.setJobId((cellInfo.getLastJobId()));
        return cellInfoDTO;
    }

    public static CellInfoDTO valueOf(CellCommit committedCellInfo){
        if (committedCellInfo == null) {
            return null;
        }

        CellInfoDTO cellInfoDTO = new CellInfoDTO();
        cellInfoDTO.setId(EntityUtils.toStr(committedCellInfo.getCellId()));
        cellInfoDTO.setContent(committedCellInfo.getContent());
        cellInfoDTO.setJobId((committedCellInfo.getLastJobId()));
        return cellInfoDTO;
    }
}
