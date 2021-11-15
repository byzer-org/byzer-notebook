package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FileInfoDTO {

    @JsonProperty("user_name")
    String userName;

    @JsonProperty("file_name")
    String fileName;

    public static FileInfoDTO valueOf(String userName, String fileName){
        FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setFileName(fileName);
        fileInfoDTO.setUserName(userName);
        return fileInfoDTO;
    }
}
