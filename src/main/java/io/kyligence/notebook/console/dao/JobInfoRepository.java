package io.kyligence.notebook.console.dao;

import io.kyligence.notebook.console.bean.entity.JobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface JobInfoRepository extends JpaRepository<JobInfo, Integer> {

    @Query(value = "select * from job_info where job_id = ?1", nativeQuery = true)
    List<JobInfo> findByJobId(String jobId);


    @Query(value = "select content from job_info where job_id = ?1", nativeQuery = true)
    String getContentByJobId(String jobId);

    @Modifying
    @Transactional
    @Query(value = "delete from job_info where job_info.finish_time < ?1 " +
            "or job_info.id in ( select id from ( select id from job_info order by id limit ?2, 100000 ) a )", nativeQuery = true)
    Integer deleteJobBefore(String time, Integer maxSize);

    @Modifying
    @Transactional
    @Query(value = "update job_info set console_log_offset = ?2 where job_id = ?1", nativeQuery = true)
    Integer updateLogOffset(String jobId, Integer offset);


}
