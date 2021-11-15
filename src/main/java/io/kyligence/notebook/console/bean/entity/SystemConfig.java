package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "system_config")
@Data
public class SystemConfig {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id = 0;  // only 1 rows

    @Column(name = "timeout", nullable = true)
    private Integer timeout;

    @Column(name = "engine", nullable = true)
    private String engine;
}
