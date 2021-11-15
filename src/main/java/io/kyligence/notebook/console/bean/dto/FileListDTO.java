package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.model.FileInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class FileListDTO {

    @JsonProperty("list")
    private List<FileDTO> list;

    public static FileListDTO valueOf(List<FileInfo> files) {
        if (files == null || files.size() == 0) {
            return null;
        }
        FileListDTO fileListDTO = new FileListDTO();
        fileListDTO.list = files.stream().map(FileDTO::valueOf).collect(Collectors.toList());
        return fileListDTO;
    }

}
