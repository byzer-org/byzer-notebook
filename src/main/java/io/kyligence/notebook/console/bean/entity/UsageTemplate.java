package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "usage_template")
@Data
public class UsageTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usage", nullable = false)
    private String usage;

    @Column(name = "template", nullable = false)
    private String template;
}
