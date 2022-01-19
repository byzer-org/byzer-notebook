package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "cell_commit")
@Data
public class CellCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cell_id")
    private Integer cellId;

    @Column(name = "notebook_id")
    private Integer notebookId;

    @Column(name = "content")
    private String content;

    @Column(name = "commit_id")
    private String commitId;

    @Column(name = "last_job_id")
    private String lastJobId;

    @Column(name = "create_time")
    private Timestamp createTime;
}
