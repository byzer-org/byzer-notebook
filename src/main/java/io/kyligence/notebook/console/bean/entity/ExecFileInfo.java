package io.kyligence.notebook.console.bean.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExecFileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "name", nullable = false)
    protected String name;

    @Column(name = "user", nullable = false)
    protected String user;

    @Column(name = "folder_id")
    protected Integer folderId;

    @Column(name = "create_time", nullable = false)
    protected Timestamp createTime;

    @Column(name = "update_time", nullable = false)
    protected Timestamp updateTime;

    private String type;

    public static List<ExecFileInfo> createArrayFiles(List<NotebookInfo> notebooks, List<WorkflowInfo> workflows) {
        List<ExecFileInfo> execFiles = new ArrayList<>();
        notebooks.forEach(notebookInfo -> {
            notebookInfo.setType("notebook");
            execFiles.add(notebookInfo);
        });
        workflows.forEach(workflowInfo -> {
            workflowInfo.setType("workflow");
            execFiles.add(workflowInfo);
        });
        return execFiles;
    }
}
