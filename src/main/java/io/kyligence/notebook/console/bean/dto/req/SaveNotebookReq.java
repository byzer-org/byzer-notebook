package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.dto.CellInfoDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SaveNotebookReq {


    @JsonProperty("cell_list")
    private List<CellInfoDTO> cellList;

    @JsonProperty("encType")
    private String encType;

    public List<CellInfoDTO> getCellList() {
        if (encType != null && encType.equals("base64")) {
            return cellList.stream().map(cell -> {
                String content = cell.getContent();
                if (StringUtils.isNotBlank(content)) {
                    try {
                        content = new String(Base64.decodeBase64(content), "UTF-8");
                        cell.setContent(content);
                    } catch (UnsupportedEncodingException e) {
                        return cell;
                    }
                }
                return cell;
            }).collect(Collectors.toList());

        }
        return cellList;
    }
}
