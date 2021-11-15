package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.dto.NodeInfoDTO;
import io.kyligence.notebook.console.support.EnumValid;
import io.kyligence.notebook.console.support.ParamEnums;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@Data
@NoArgsConstructor
public class CUNodeReq {

    @EnumValid(enumClass = ParamEnums.NodeType.class, nullable = false,
            ignoreEmpty = false, message = "ParamError: invalid node type")
    @JsonProperty("type")
    private String nodeType;

    @Valid
    @JsonProperty("content")
    private NodeInfoDTO.NodeContent content;

    @JsonProperty("position")
    private NodeInfoDTO.NodePosition position;
}
