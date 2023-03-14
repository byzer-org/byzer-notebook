package io.kyligence.notebook.console.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kyligence.notebook.console.bean.dto.UserParamsDTO;
import io.kyligence.notebook.console.bean.model.ScheduleSetting;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CreateScheduleReq {
    @Pattern(regexp = "[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "The schedule name can only contain numbers, letters, Chinese characters, and underscores")
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @NotBlank
    @JsonProperty("entity_type")
    private String entityType;

    @NotNull
    @JsonProperty("entity_id")
    private Integer entityId;

    @JsonProperty("scheduler_id")
    private Integer schedulerId;

    @JsonProperty("commit_id")
    private String commitId;

    @JsonProperty("schedule")
    private ScheduleSetting schedule;

    @JsonProperty("extra")
    private Map<String, String> extra;

    @JsonProperty("task_name")
    private String taskName;

    @JsonProperty("task_desc")
    private String taskDesc;

    @JsonProperty("task_timeout")
    private Integer taskTimeout;

    @JsonProperty("user_params")
    private List<UserParamsDTO> userParams;
}
