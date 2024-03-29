package io.kyligence.notebook.console.notification;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.EngineService;
import liquibase.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @Author; Andie Huang
 * @Date: 2022/6/28 18:23
 */
@Service
@Slf4j
public class NotificationService {

    private static final String NOTIFICATION_SQL = "select 1 as col1 as a;RUN a as FeishuMessageExt.`` where text=\"%s\" AND webhook = \"%s\" as A2;";

    private static final NotebookConfig config = NotebookConfig.getInstance();

    @Autowired
    EngineService engineService;

    public void notification(String notebookName, JobInfo jobInfo, long duration, String user) {
        String webHook = config.getNitificationWebhook();
        String header = config.getNitificationMsgHeader();

        boolean notificationMessageEnabled = false;
        try {
            notificationMessageEnabled = Boolean.parseBoolean(config.notificationMessageEnabled());
        }
        catch(Exception e){
            log.warn("Invalid value - {} of notebook.scheduler.notification.message.enabled, should be true or false",
                    config.notificationMessageEnabled());
        }

        if (StringUtil.isEmpty(webHook)) {
            log.warn("[NotificationService] The webhook info is not set. Skip sending IM notifications !");
            return;
        }

        try {

            String jobStatusStr = "finished";
            if (jobInfo.getStatus() == JobInfo.JobStatus.FAILED) {
                jobStatusStr = "failed";
            } else if (jobInfo.getStatus() == JobInfo.JobStatus.SUCCESS) {
                jobStatusStr = "succeed";
            }

            long totalSecs = duration / 1000;

            long hours = totalSecs / 3600;
            long minutes = (totalSecs % 3600) / 60;
            long seconds = totalSecs % 60;

            String durString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
            String body = String.format("- Notebook Name: %s\n" +
                    "- Schedule Name: %s\n" +
                    "- Schedule Time: %s\n" +
                    "- Duration: %s \n" +
                    "- Execute User: %s\n" +
                    "- Status: %s",
                    notebookName, jobInfo.getName(), time, durString, user, jobStatusStr);
            if( notificationMessageEnabled ) {
                body = String.format(body + System.lineSeparator() + "- Message: %s", jobInfo.getMsg());
            }

            String msg = header + "\n" + body;
            String sql = String.format(NOTIFICATION_SQL, msg, webHook);
            engineService.runScript(new EngineService.RunScriptParams()
                    .withSql(sql));
        } catch (Exception ex) {
            log.warn("[NotificationService] Exceptions occurred when sending IM notifications." + ex.getMessage());
            throw new ByzerException(ErrorCodeEnum.SENDING_IM_ERROR);
        }
    }
}
