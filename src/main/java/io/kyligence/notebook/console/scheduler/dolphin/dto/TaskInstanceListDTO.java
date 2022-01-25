package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TaskInstanceListDTO {
    private String msg;

    private Integer code;

    private Body data;

    @Data
    @NoArgsConstructor
    public static class Body {

        private List<TaskInstance> taskList;
        private String processInstanceState;
    }
}
