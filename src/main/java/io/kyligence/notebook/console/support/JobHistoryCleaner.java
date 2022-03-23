package io.kyligence.notebook.console.support;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.dao.JobInfoArchiveRepository;
import io.kyligence.notebook.console.dao.JobInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.util.Date;

@Configuration
@EnableScheduling
@Slf4j
public class JobHistoryCleaner {

    private NotebookConfig notebookConfig = NotebookConfig.getInstance();

    private Integer maxSize = Integer.parseInt(notebookConfig.getJobHistorySize());

    private Long maxTime = Long.valueOf(notebookConfig.getJobHistoryTime());

    private Long archiveTime = Long.valueOf(notebookConfig.getJobArchiveTime());

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Autowired
    private JobInfoArchiveRepository jobInfoArchiveRepository;

    @Scheduled(cron = "0 0 1 * * ? ")
    private void archive() {
        Timestamp time = new Timestamp(System.currentTimeMillis() - archiveTime * 24 * 60 * 60 * 1000);
        Integer archived = jobInfoRepository.archiveJobInfo(String.valueOf(time));
        log.info("archived {} job history from [job_info] to [job_info_archive] at {}", archived, new Date());

        Integer deleted = jobInfoRepository.cleanJobInfo(String.valueOf(time));
        log.info("clean {} job history from [job_info] at {}", deleted, new Date());
    }

    @Scheduled(cron = "0 0 2 * * ? ")
    private void clean() {
        Timestamp time = new Timestamp(System.currentTimeMillis() - maxTime * 24 * 60 * 60 * 1000);
        Integer effectedNum = jobInfoRepository.deleteJobBefore(String.valueOf(time), maxSize);
        log.info("clean {} job history from [job_info_archive] at {}", effectedNum, new Date());
        effectedNum = jobInfoArchiveRepository.archiveRunningJobs(String.valueOf(time));
        log.info("mark {} [RUNNING] job history as [KILLED] from [job_info_archive] at {}", effectedNum, new Date());
    }


}
