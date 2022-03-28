package io.kyligence.notebook.console.tools;


import io.kyligence.notebook.console.bean.dto.IdNameTypeDTO;
import lombok.Data;

import java.util.List;

@Data
public class ImportResponseDTO {
    private String code;
    private List<IdNameTypeDTO> data;
    private String msg;
}