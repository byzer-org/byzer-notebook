package io.kyligence.notebook.console.bean.dto;

import io.kyligence.notebook.console.bean.model.JobProgress;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


@Data
@NoArgsConstructor
public class JobProgressDTO {
    private Integer completedJobs;
    private Integer inProgressJobs;
    private Integer failedJobs;
    private Double progress;

    public static JobProgressDTO valueOf(JobProgress jobProgress) {
        JobProgressDTO dto = new JobProgressDTO();
        int completedJobsNum = 0;
        int inProgressJobsNum = 0;
        int failedJobsNum = 0;
        if (jobProgress != null && jobProgress.getActiveJobs() != null && !jobProgress.getActiveJobs().isEmpty()) {

            for (JobProgress.ActiveJob activeJob : jobProgress.getActiveJobs()) {
                if ((activeJob.getNumFailedTasks() != null && activeJob.getNumFailedTasks() > 0) ||
                        (activeJob.getNumFailedStages() != null && activeJob.getNumFailedStages() > 0)) {
                    failedJobsNum += 1;
                    continue;
                }
                if (StringUtils.isEmpty(activeJob.getCompletionTime())) {
                    inProgressJobsNum += 1;
                } else {
                    completedJobsNum += 1;
                }
            }
        }
        dto.setCompletedJobs(completedJobsNum);
        dto.setInProgressJobs(inProgressJobsNum);
        dto.setFailedJobs(failedJobsNum);
        int totalJobsNum = completedJobsNum + inProgressJobsNum + failedJobsNum;
        dto.setProgress(totalJobsNum == 0 ? 0 : (double) (completedJobsNum + failedJobsNum) / (double) totalJobsNum);
        return dto;
    }

}
