package io.kyligence.notebook.console.scheduler;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class SchedulerConfig {

    private String schedulerName;

    private String schedulerUrl;

    private String callbackUrl;

    private String callbackToken;

    private String authToken;

    private String defaultProjectName;

    private String defaultWarningType;

    private Integer defaultWarningGroupId;

    private String defaultWorker;

    private String defaultFailureStrategy;

    private String defaultProcessInstancePriority;

}
