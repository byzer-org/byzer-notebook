package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.util.EntityUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DemoInfoDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("commit_id")
    private String commitId;

    @JsonProperty("is_demo")
    private Boolean isDemo;

    public static DemoInfoDTO valueOf(Integer id, String name, String type, String commitId) {
        DemoInfoDTO result = new DemoInfoDTO();
        result.setId(EntityUtils.toStr(id));
        result.setName(name);
        result.setType(type);
        result.setIsDemo(true);
        result.setCommitId(commitId);
        return result;
    }
}
