package io.kyligence.notebook.console.bean.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileInfo {

    private String group;

    private int length;

    private String modification_time;

    private String name;

    private String owner;

    private String path;

    private String permission;

    private String replication;
}
