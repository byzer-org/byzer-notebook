package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "user_action")
@Data
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user", nullable = false)
    private String user;

    @Column(name = "opened_notebooks")
    private String openedNotebooks;

    @Column(name = "uploaded_files")
    private String uploadedFiles;
}
