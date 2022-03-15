package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "et_params_def")
@Data
public class ETParamsDef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "et_id", nullable = false)
    private Integer etId;

//    @Column(name = "et_usage", nullable = false)
//    private String etUsage;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "optional")
    private Boolean optional;

    @Column(name = "type")
    private String type;

    @Column(name = "value_type")
    private String valueType;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "enum_values")
    private String enumValues;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "label")
    private String label;

    @Column(name = "depends")
    private String depends;

    @Column(name = "`constraint`")
    private String constraint;

}
