package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class UserJoinReq {

    @Pattern(regexp = "^[0-9a-zA-Z_@.-]+$", message = "the username can only contains numbers, letters and underscores.")
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

}
