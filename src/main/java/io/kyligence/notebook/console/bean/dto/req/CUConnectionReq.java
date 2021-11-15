package io.kyligence.notebook.console.bean.dto.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.dto.ConnectionDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;


@Data
@NoArgsConstructor
public class CUConnectionReq {

    @Length(min = 1, max = 50, message = "Connection name should less than 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_]*$", message = "Connection name only support English characters, numbers and underlines")
    @JsonProperty("name")
    private String connectionName;

    @JsonProperty("content")
    private ConnectionDTO content;
}
