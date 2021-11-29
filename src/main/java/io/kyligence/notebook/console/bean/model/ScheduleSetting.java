package io.kyligence.notebook.console.bean.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.scheduler.dolphin.dto.ScheduleInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class ScheduleSetting {

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("crontab")
    private String crontab;

    public static ScheduleSetting valueOf(ScheduleInfo scheduleInfo){
        if (Objects.isNull(scheduleInfo)) return null;
        ScheduleSetting schedule = new ScheduleSetting();
        schedule.setCrontab(scheduleInfo.getCrontab());
        schedule.setEndTime(scheduleInfo.getEndTime());
        schedule.setStartTime(scheduleInfo.getStartTime());
        return schedule;
    }
}
