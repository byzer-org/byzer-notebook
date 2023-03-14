package io.kyligence.notebook.console.bean.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserParamsDTO {
    private String prop;
    private String value;

    public static UserParamsDTO valueOf(UserParamsDTO dto) {
        UserParamsDTO userParamsDTO = new UserParamsDTO();
        userParamsDTO.prop = dto.prop;
        userParamsDTO.value = dto.value;
        return userParamsDTO;
    }
}
