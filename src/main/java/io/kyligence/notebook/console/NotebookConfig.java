package io.kyligence.notebook.console;

import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.scheduler.SchedulerConfig;
import io.kyligence.notebook.console.util.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class  NotebookConfig {

    public final static NotebookConfig INSTANCE = new NotebookConfig();

    private Properties properties = new Properties();

    private static final String PROPERTIES_FILE = "notebook.properties";

    private static final String OVERRIDE_PROPERTIES_FILE = "notebook.override.properties";

    private static final String intranetIp = NetworkUtils.getIntranetIp();

    private NotebookConfig() {
        loadConfig();
    }

    public static NotebookConfig getInstance() {
        return INSTANCE;
    }

    public void loadConfig() {
        File propFile = getPropertiesFile();
        if (!propFile.exists()) {
            log.error("fail to locate {}", PROPERTIES_FILE);
            throw new ByzerException(ErrorCodeEnum.LOAD_CONFIG_ERROR, PROPERTIES_FILE);
        }

        Properties conf = new Properties();
        FileInputStream is = null;
        FileInputStream ois = null;
        try {
            is = new FileInputStream(propFile);
            conf.load(is);

            File propOverrideFile = new File(propFile.getParentFile(), propFile.getName() + ".override");
            if (propOverrideFile.exists()) {
                ois = new FileInputStream(propOverrideFile);
                Properties propOverride = new Properties();
                propOverride.load(ois);
                conf.putAll(propOverride);
            }
        } catch (IOException e) {
            throw new ByzerException(e.getMessage());
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
            if (ois != null) {
                IOUtils.closeQuietly(ois);
            }
        }

        this.properties = conf;
    }

    private File getPropertiesFile() {
        String path = System.getProperty("PROPERTIES_PATH");
        if (StringUtils.isBlank(path)) {
            path = getPropertiesDirPath();
        }
        File overrideFile = new File(path, OVERRIDE_PROPERTIES_FILE);
        if (overrideFile.exists()) {
            return overrideFile;
        } else {
            return new File(path, PROPERTIES_FILE);
        }
    }

    private String getNotebookHome() {
        String notebookHome = System.getProperty("NOTEBOOK_HOME");
        if (StringUtils.isBlank(notebookHome)) {
            notebookHome = System.getenv("NOTEBOOK_HOME");
            if (StringUtils.isBlank(notebookHome)) {
                throw new ByzerException(ErrorCodeEnum.ENV_NOT_FOUND);
            }
        }
        return notebookHome;
    }

    public String getOptional(String propertyKey, String defaultValue) {
        String property = System.getProperty(propertyKey);
        if (!StringUtils.isBlank(property)) {
            return property.trim();
        }
        property = properties.getProperty(propertyKey);
        if (StringUtils.isBlank(property)) {
            return defaultValue.trim();
        } else {
            return property.trim();
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getPropertiesDirPath() {
        return getNotebookHome() + File.separator + "conf";
    }

    public int getLogHttpErrorTime() {
        return Integer.parseInt(getOptional("insight.semantic.log.http.debug", "10000"));
    }

    public String getMlsqlClusterUrl() {
        return getOptional("notebook.mlsql.cluster-url",  "");
    }

    public String getMlsqlEngineUrl() {
        return getOptional("notebook.mlsql.engine-url",  "http://" + intranetIp + ":9003");
    }

    public String getMlsqlEngineBackupUrl() {
        return getOptional("notebook.mlsql.engine-backup-url",  "http://" + intranetIp + ":9004");
    }

    public String getMlsqlCloudUrl() {
        return getOptional("notebook.mlsql.cloud-url",  "http://" + intranetIp + ":8090");
    }

    public String getNotebookUrl() {
        return getOptional("notebook.url",  "http://" + intranetIp + ":9002");
    }

    public String getAuthClient() {
        return getOptional("notebook.mlsql.auth-client",  "streaming.dsl.auth.client.DefaultConsoleClient");
    }

    public String getUserHome() {
        return getOptional("notebook.user.home", "/mlsql");
    }

    public String getExecutionTimeoutMillonSeconds() {
        int timeoutMinute =  Integer.parseInt(getOptional("notebook.execution.timeout", "2880"));
        if (timeoutMinute == -1) {
            return "-1";
        }
        return String.valueOf(timeoutMinute * 60 * 1000);
    }

    public String getExecutionEngine() {
        return getOptional("notebook.execution.engine", "default");
    }

    public String getExecutionEngineUrl() {
        if ("default".equals(getExecutionEngine())) {
            return getMlsqlEngineUrl();
        }
        return getMlsqlEngineBackupUrl();
    }

    public String getJobHistorySize(){
        return getOptional("notebook.job.history.max-size", "2000000");
    }

    public String getJobHistoryTime(){
        return getOptional("notebook.job.history.max-time", "30");
    }

    public void updateConfig(String name, String value) {
        properties.setProperty(name, value);
    }

    public Double getUserTotalFileSizeLimit() {
        return Double.parseDouble(getOptional("notebook.user.resource.total-file-size-limit-kb", "0"));
    }

    public Double getUserFileSizeLimit() {
        return Double.parseDouble(getOptional("notebook.user.resource.file-size-limit-kb", "0"));
    }

    public Integer getUserNoteBookNumLimit(){
        return Integer.parseInt(getOptional("notebook.user.resource.notebook-num-limit", "0"));
    }

    public Integer getUserWorkflowNumLimit(){
        return Integer.parseInt(getOptional("notebook.user.resource.workflow-num-limit", "0"));
    }

    public String getVersionPath(){
        return getNotebookHome() + File.separator + "VERSION";
    }

    public String getCommitSHAPath(){
        return getNotebookHome() + File.separator + "commit_SHA1";
    }

    public String getFrontendCommitSHAPath(){
        return getNotebookHome() + File.separator + "commit_SHA1.frontend";
    }

    public Boolean getIsTrial(){
        return Objects.equals(getOptional("notebook.env.is-trial", ""), "true");
    }

    public String getSecretKey(){return getOptional("notebook.security.key", "6173646661736466e4bda0e8bf983161");}

    public String getOutputSize(){return getOptional("notebook.job.output-size", "1000");}

    public String getScheduleCallbackUser(){return getOptional("notebook.scheduler.callback-user", "ByzerRobot");}

    public String getScheduleCallbackToken(){return getOptional("notebook.scheduler.callback-token", "6173646661736466e4bda0e8bf983161");}


    public Boolean getIsSchedulerEnabled(){
       return Objects.equals(getOptional("notebook.scheduler.enable", "").trim(), "true");
    }

    public List<SchedulerConfig> getSchedulerConfig() {

        SchedulerConfig config = new SchedulerConfig();
        config.setSchedulerName(getOptional("notebook.scheduler.scheduler-name", ""));
        config.setSchedulerUrl(getOptional("notebook.scheduler.scheduler-url", ""));
        config.setAuthToken(getOptional("notebook.scheduler.auth-token", ""));
        config.setCallbackUrl(getNotebookUrl() + "/api/schedule/execution");
        config.setCallbackToken(getScheduleCallbackToken());
        config.setDefaultProjectName(getOptional("notebook.scheduler.project-name", "ByzerScheduler"));
        config.setDefaultWarningType(getOptional("notebook.scheduler.warning-type", "ALL"));
        config.setDefaultWarningGroupId(Integer.valueOf(getOptional("notebook.scheduler.warning-group-id", "1")));
        config.setDefaultFailureStrategy(getOptional("notebook.scheduler.failure-strategy", "END"));
        config.setDefaultProcessInstancePriority(getOptional("notebook.scheduler.instance-priority", "MEDIUM"));
        config.setDefaultWorker(getOptional("notebook.scheduler.worker", "default"));
        List<SchedulerConfig> r = Lists.newArrayList();
        r.add(config);
        return r;
    }
}
