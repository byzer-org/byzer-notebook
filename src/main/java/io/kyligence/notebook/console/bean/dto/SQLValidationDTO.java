package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SQLValidationDTO {
    @JsonProperty("result")
    private String result;

    @JsonProperty("msg")
    private String msg;

    public static SQLValidationDTO valueOf(boolean success, String message){
        SQLValidationDTO sqlValidationDTO = new SQLValidationDTO();
        if (success){
            sqlValidationDTO.setResult("success");
        } else {
            sqlValidationDTO.setResult("error");
            sqlValidationDTO.setMsg(message);
        }
        return sqlValidationDTO;
    }
}
