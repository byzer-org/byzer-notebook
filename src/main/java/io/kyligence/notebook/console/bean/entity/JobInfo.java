package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "job_info")
@Data
public class JobInfo {

    public JobInfo() {

    }

    // TODO, fix type query, fix timezone
    public JobInfo(String jobId, Integer status, String user, String notebook, String engine,
                   java.util.Date createTime, java.util.Date finishTime) {
        this.jobId = jobId;
        this.status = status;
        this.user = user;
        this.notebook = notebook;
        this.engine = engine;
        this.createTime = (Timestamp) createTime;
        this.finishTime = (Timestamp) finishTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "notebook", nullable = false)
    private String notebook;

    @Column(name = "engine", nullable = false)
    private String engine;

    @Column(name = "content", nullable = false)
    private String content;

    /*
       0: Running
       1: Success
       2: Failed
       3: Killed
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "user", nullable = false)
    private String user;

    @Column(name = "create_time", nullable = false)
    private Timestamp createTime;

    @Column(name = "finish_time", nullable = false)
    private Timestamp finishTime;

    @Column(name = "msg")
    private String msg;

    @Column(name = "job_progress")
    private String jobProgress;

    @Column(name = "result")
    private String result;

    @Column(name = "console_log_offset")
    private Integer consoleLogOffset;

    public interface JobStatus {
        int RUNNING = 0;
        int SUCCESS = 1;
        int FAILED = 2;
        int KILLED = 3;
    }

}
