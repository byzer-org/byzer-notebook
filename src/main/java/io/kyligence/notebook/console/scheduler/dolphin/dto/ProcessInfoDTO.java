package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ProcessInfoDTO {
    private String msg;

    private Integer code;

    private ProcessInfo data;
}
