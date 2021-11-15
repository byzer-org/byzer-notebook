package io.kyligence.notebook.console.support;

import io.kyligence.notebook.console.NotebookConfig;
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

    @Autowired
    private JobInfoRepository jobInfoRepository;

    @Scheduled(cron = "0 0 2 * * ? ")
    private void clean() {
        Timestamp time = new Timestamp(System.currentTimeMillis() - maxTime * 24 * 60 * 60 * 1000);
        Integer effectedNum = jobInfoRepository.deleteJobBefore(String.valueOf(time), maxSize);
        log.info("clean {} job history at {}", effectedNum, new Date());
    }


}
