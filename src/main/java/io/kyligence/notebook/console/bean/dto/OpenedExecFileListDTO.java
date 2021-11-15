package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.UserAction;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class OpenedExecFileListDTO {

    @JsonProperty("list")
    private List<OpenedExecFileDTO> list;

    public static OpenedExecFileListDTO valueOf(UserAction userAction) {
        OpenedExecFileListDTO openedExecFileListDTO = new OpenedExecFileListDTO();
        if (userAction == null || StringUtils.isEmpty(userAction.getOpenedNotebooks())) {
            return openedExecFileListDTO;
        }

        List<OpenedExecFileDTO> openedNotebooks = JacksonUtils.readJsonArray(userAction.getOpenedNotebooks(), OpenedExecFileDTO.class);
        openedExecFileListDTO.list = openedNotebooks;
        return openedExecFileListDTO;
    }
}
