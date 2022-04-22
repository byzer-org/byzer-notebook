package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "system_config")
@Data
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "`user`")
    private String user;

    @Column(name = "engine")
    private String engine;
}
