package io.kyligence.notebook.console.bean.dto.req;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SettingsUpdateReq {

    private Integer timeout;

    private String engine;
}
