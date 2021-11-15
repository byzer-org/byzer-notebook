package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "node_def_info")
@Data
public class NodeDefInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "node_type")
    private String nodeType;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
