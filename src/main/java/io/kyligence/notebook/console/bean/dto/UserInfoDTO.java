package io.kyligence.notebook.console.bean.dto;

import io.kyligence.notebook.console.bean.entity.UserInfo;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoDTO {

    private String id;

    private String username;

    public static UserInfoDTO valueOf(UserInfo userInfo) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(EntityUtils.toStr(userInfo.getId()));
        userInfoDTO.setUsername(userInfo.getName());
        return userInfoDTO;
    }
}
