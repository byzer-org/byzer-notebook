package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class InstanceLogDTO {
    private String msg;

    private Integer code;

    private String data;
}
