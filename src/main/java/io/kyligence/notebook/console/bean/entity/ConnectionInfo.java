package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "connection_info")
@Data
public class ConnectionInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "datasource", nullable = false)
    private String datasource;

    @Column(name = "name")
    private String name;

    @Column(name = "driver", nullable = false)
    private String driver;

    @Column(name = "user", nullable = false)
    private String user;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "username", nullable = false)
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "parameter")
    private String parameter;

    @Column(name = "create_time", nullable = false)
    private Timestamp createTime;

    @Column(name = "update_time", nullable = false)
    private Timestamp updateTime;
}
