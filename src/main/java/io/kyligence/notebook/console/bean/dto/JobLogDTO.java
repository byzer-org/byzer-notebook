package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.JobLog;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class JobLogDTO {

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("offset")
    private Long offset;

    @JsonProperty("logs")
    private List<String> logs;


    public static JobLogDTO valueOf(String jobId, JobLog jobLog) {
        if (jobLog == null) {
            return null;
        }

        JobLogDTO resp = new JobLogDTO();
        resp.jobId = jobId;
        resp.offset = jobLog.getOffset();
        resp.logs = jobLog.getValue();
        return resp;
    }
}
