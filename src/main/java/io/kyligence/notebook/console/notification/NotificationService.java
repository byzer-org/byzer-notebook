package io.kyligence.notebook.console.notification;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.entity.JobInfo;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.EngineService;
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

    private final String NOTIFICATION_SQL = "select 1 as col1 as a;RUN a as FeishuMessageExt.`` where text=\"%s\" AND webhook = \"%s\" as A2;";

    private static final NotebookConfig config = NotebookConfig.getInstance();

    @Autowired
    EngineService engineService;

    public void notification(String scheduleName, long duration, String user, int status) {
        String webHook = config.getNitificationWebhook();
        String header = config.getNitificationMsgHeader();

        try {

            String jobStatusStr = "finished";
            if (status == JobInfo.JobStatus.FAILED) {
                jobStatusStr = "failed";
            } else if (status == JobInfo.JobStatus.SUCCESS) {
                jobStatusStr = "successed";
            }

            long totalSecs = duration / 1000;

            long hours = totalSecs / 3600;
            long minutes = (totalSecs % 3600) / 60;
            long seconds = totalSecs % 60;

            String durString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
            String body = String.format("- Schedule Name: %s\n" +
                    "- Schedule Time: %s\n" +
                    "- Duration: %s ms\n" +
                    "- Execute User: %s\n" +
                    "- Status: %s", scheduleName, time, durString, user, jobStatusStr);
            String msg = header + "\n" + body;
            String sql = String.format(NOTIFICATION_SQL, msg, webHook);
            String responseBody = engineService.runScript(new EngineService.RunScriptParams()
                    .withSql(sql));
        } catch (Exception ex) {
            // 失败的话抛出异常么？？
            throw new ByzerException(ErrorCodeEnum.SENDING_IM_ERROR);
        }
    }
}
