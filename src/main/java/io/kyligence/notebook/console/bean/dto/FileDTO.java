package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.FileInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
@Data
public class FileDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("type")
    private String type;

    @JsonProperty("upload")
    private Boolean upload;

    public static FileDTO valueOf(FileInfo fileInfo) {
        FileDTO fileDTO = new FileDTO();
        fileDTO.name = fileInfo.getName();
        fileDTO.path = fileInfo.getPath();
        fileDTO.type = fileInfo.getPermission().startsWith("d") ? "folder" : "file";
        fileDTO.upload = StringUtils.contains(fileInfo.getPath(), "/tmp/upload/");
        return fileDTO;
    }

}