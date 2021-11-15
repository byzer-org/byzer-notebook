package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "param_def_info")
@Data
public class ParamDefInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "node_def_id")
    private Integer nodeDefId;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "value_type")
    private String valueType;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "constrain")
    private String constrain;

    @Column(name = "bind")
    private Integer bind;

    @Column(name = "is_group_param")
    private Boolean isGroupParam;

}
