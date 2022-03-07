package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ProcessInstanceDetailDTO {
    private String msg;

    private Integer code;

    private ProcessInstance data;

}