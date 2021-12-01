package io.kyligence.notebook.console.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VersionInfo {
    @JsonProperty("backend_commit_SHA")
    private String backendCommitSHA;

    @JsonProperty("frontend_commit_SHA")
    private String frontendCommitSHA;

    @JsonProperty("build_time")
    private long buildTime;

    private String version;

    public static VersionInfo valueOf(long buildTime, String frontendCommitSHA, String backendCommitSHA, String version) {
        VersionInfo info = new VersionInfo();
        info.setVersion(version);
        info.setBuildTime(buildTime);
        info.setBackendCommitSHA(backendCommitSHA);
        info.setFrontendCommitSHA(frontendCommitSHA);
        return info;
    }
}
