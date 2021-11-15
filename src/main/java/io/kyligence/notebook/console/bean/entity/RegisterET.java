package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "register_et")
@Data
public class RegisterET {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "caption")
    private String caption;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "et_usage")
    private String etUsage;

    @Column(name = "enable")
    private Boolean enable;
}
