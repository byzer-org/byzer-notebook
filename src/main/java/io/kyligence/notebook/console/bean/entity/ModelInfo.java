package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "model_info")
@Data
public class ModelInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "workflow_id", nullable = false)
    private Integer workflowId;

    @Column(name = "algorithm")
    private String algorithm;

    @Column(name = "path")
    private String path;

    @Column(name = "group_size")
    private Integer groupSize;

    @Column(name = "node_id")
    private Integer nodeId;

    @Column(name = "user_name")
    private String userName;
}
