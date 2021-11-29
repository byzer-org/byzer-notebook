package io.kyligence.notebook.console.scheduler.dolphin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInfo {
    private int id;
    private int userId;
    private String userName;
    private String name;
    private String description;
    private String createTime;
    private String updateTime;
    private int perm;
    private int defCount;
    private int instRunningCount;
}
