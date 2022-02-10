package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "workflow_commit")
public class WorkflowCommit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "workflow_id", nullable = false)
    private Integer workflowId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "commit_id")
    private String commitId;

    @Column(name = "create_time", nullable = false)
    private Timestamp createTime;
}
