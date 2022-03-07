package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class BasicDataAck {
    private String msg;

    private Integer code;

    private Map<String, Object> data;
}
