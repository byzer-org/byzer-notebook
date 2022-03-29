package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.JobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;


public interface JobInfoRepository extends JpaRepository<JobInfo, Integer> {

    @Query(value = "select * from job_info where job_id = ?1", nativeQuery = true)
    List<JobInfo> findByJobId(String jobId);

    @Query(value = "select `status` from job_info where job_id = ?1", nativeQuery = true)
    Integer getJobStatus(String jobId);

    @Query(value = "select content from job_info where job_id = ?1", nativeQuery = true)
    String getContentByJobId(String jobId);


    @Query(value = "select create_time from job_info where job_id = ?1", nativeQuery = true)
    Timestamp getJobStartTime(String jobId);

    @Modifying
    @Transactional
    @Query(value = "delete from job_info_archive where job_info_archive.finish_time < ?1 " +
            "or job_info_archive.id in (select `id` from (select `id` from job_info_archive order by `id` limit ?2, 100000) a)", nativeQuery = true)
    Integer deleteJobBefore(String time, Integer maxSize);

    @Modifying
    @Transactional
    @Query(value = "insert into job_info_archive (select * from job_info where job_info.create_time < ?1 and " +
            "`user` != 'admin')", nativeQuery = true)
    Integer archiveJobInfo(String time);

    @Modifying
    @Transactional
    @Query(value = "delete from job_info where job_info.create_time < ?1 and `user` != 'admin'", nativeQuery = true)
    Integer cleanJobInfo(String time);

}
