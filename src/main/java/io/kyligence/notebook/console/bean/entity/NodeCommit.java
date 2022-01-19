package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "node_commit")
@Data
public class NodeCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "workflow_id", nullable = false)
    private Integer workflowId;

    @Column(name = "node_id", nullable = false)
    private Integer nodeId;

    @Column(name = "content")
    private String content;

    @Column(name = "user", nullable = false)
    private String user;

    @Column(name = "type")
    private String type;

    @Column(name = "position")
    private String position;

    @Column(name = "input")
    private String input;

    @Column(name = "output")
    private String output;

    @Column(name = "commit_id")
    private String commitId;

    @Column(name = "create_time")
    private Timestamp createTime;
}
