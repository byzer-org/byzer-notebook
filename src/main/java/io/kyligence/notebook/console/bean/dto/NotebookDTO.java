package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.CellInfo;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class NotebookDTO extends ExecFileDTO{

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("user")
    private String user;

    @JsonProperty("cell_list")
    private List<CellInfoDTO> cellList;


    public static NotebookDTO valueOf(NotebookInfo notebookInfo, List<Integer> cellIds, List<CellInfo> cellInfos) {
        if (notebookInfo == null) {
            return null;
        }

        NotebookDTO notebookDTO = new NotebookDTO();
        notebookDTO.setId(EntityUtils.toStr(notebookInfo.getId()));
        notebookDTO.setName(notebookInfo.getName());
        notebookDTO.setUser(notebookInfo.getUser());

        if (cellIds != null && !cellIds.isEmpty() && cellInfos != null) {
            Map<Integer, CellInfo> cellInfoMap = new HashMap<>();
            cellInfos.forEach(cellInfo -> cellInfoMap.put(cellInfo.getId(), cellInfo));

            List<CellInfoDTO> cellInfoDTOS = cellIds.stream().map(cellId -> {
                CellInfo cellInfo = cellInfoMap.get(cellId);
                return CellInfoDTO.valueOf(cellInfo);
            }).collect(Collectors.toList());
            notebookDTO.setCellList(cellInfoDTOS);
        }
        return notebookDTO;
    }

    public static NotebookDTO valueOf(NotebookInfo notebookInfo) {
        return valueOf(notebookInfo, null, null);
    }

}
