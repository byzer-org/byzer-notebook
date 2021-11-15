package io.kyligence.notebook.console.bean.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CurrentJobInfo {

    private String owner;

    private String jobType;

    private String jobName;

    private String jobContent;

    private String groupId;

    private Long startTime;

    private Long timeout;

    private Progress progress;


    @Data
    @NoArgsConstructor
    public static class Progress {

        private Integer totalJob;

        private Integer currentJobIndex;

        private String script;
    }
}
