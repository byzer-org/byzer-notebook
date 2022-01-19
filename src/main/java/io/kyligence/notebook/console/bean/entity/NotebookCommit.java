package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "notebook_commit")
public class NotebookCommit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "notebook_id", nullable = false)
    private Integer notebookId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cell_list", nullable = false)
    private String cellList;

    @Column(name = "commit_id")
    private String commitId;

    @Column(name = "create_time", nullable = false)
    private Timestamp createTime;
}
