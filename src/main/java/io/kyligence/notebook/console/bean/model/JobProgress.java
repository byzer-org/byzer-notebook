package io.kyligence.notebook.console.bean.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JobProgress {

    private String groupId;

    private Integer activeJobsNum;

    private Integer completedJobsNum;

    private Integer failedJobsNum;

    private List<ActiveJob> activeJobs;

    @Data
    @NoArgsConstructor
    public static class ActiveJob {

        private Integer jobId;

        private String submissionTime;

        private String completionTime;

        private Integer numTasks;

        private Integer numActiveTasks;

        private Integer numCompletedTasks;

        private Integer numSkippedTasks;

        private Integer numFailedTasks;

        private Integer numKilledTasks;

        private Integer numCompletedIndices;

        private Integer numActiveStages;

        private Integer numCompletedStages;

        private Integer numSkippedStages;

        private Integer numFailedStages;

        private Integer duration;
    }
}
