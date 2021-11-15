package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegisterAndResetDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("password")
    private String password;

}
