package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.entity.UserInfo;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoDTO {

    private String id;

    private String username;

    @JsonProperty("is_admin")
    private Boolean isAdmin;

    public static UserInfoDTO valueOf(UserInfo userInfo) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(EntityUtils.toStr(userInfo.getId()));
        userInfoDTO.setUsername(userInfo.getName());
        userInfoDTO.setIsAdmin(userInfo.getName().equalsIgnoreCase("admin"));
        return userInfoDTO;
    }
}
