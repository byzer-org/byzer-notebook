package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
public class TaskTimeoutDTO {
    private Integer maxRetryTimes;
    private Integer retryInterval;
    private Integer timeout;

    public static TaskTimeoutDTO valueOf(Integer maxRetryTimes, Integer retryInterval, Integer timeout){
        TaskTimeoutDTO dto = new TaskTimeoutDTO();
        dto.setRetryInterval(retryInterval);
        dto.setTimeout(timeout);
        dto.setMaxRetryTimes(maxRetryTimes);
        return dto;
    }

    public static TaskTimeoutDTO parseFrom(Map<String, String> extraSettings){
        if (Objects.isNull(extraSettings)) return valueOf(3, 1, 0);
        TaskTimeoutDTO dto = new TaskTimeoutDTO();
        dto.setTimeout(!extraSettings.containsKey("timeout") ? 0 : Integer.parseInt(extraSettings.get("timeout")));
        dto.setMaxRetryTimes(!extraSettings.containsKey("max_retry_times") ? 3 : Integer.parseInt(extraSettings.get("max_retry_times")));
        dto.setRetryInterval(!extraSettings.containsKey("retry_interval") ? 1 : Integer.parseInt(extraSettings.get("retry_interval")));
        return dto;
    }
}
