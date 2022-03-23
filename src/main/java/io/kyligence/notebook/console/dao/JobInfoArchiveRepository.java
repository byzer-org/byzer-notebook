package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.JobInfoArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface JobInfoArchiveRepository extends JpaRepository<JobInfoArchive, Integer> {
    @Modifying
    @Transactional
    @Query(value = "update job_info_archive set `status` = 3 where `create_time` < ?1 and (`status` = 0 or `status` > 3)", nativeQuery = true)
    Integer archiveRunningJobs(String time);

    @Query(value = "select content from job_info_archive where job_id = ?1", nativeQuery = true)
    String getContentByJobId(String jobId);
}
