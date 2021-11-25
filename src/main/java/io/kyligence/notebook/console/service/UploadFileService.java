package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.model.UploadedFiles;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UploadFileService {

    @Autowired
    private EngineService engineService;

    @Autowired
    private UserService userService;

    private static final NotebookConfig config = NotebookConfig.getInstance();

    public void uploadFileToHdfs(String fileName) {
        log.info("Upload File to HDFS: "+ fileName);
        String sql = "run command as DownloadExt.`` where from=\"" + fileName + "\" and to=\"/tmp/upload\";";
        engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql));

    }

    public void uploadFileToHdfs(String user, String fileName, long fileSize) {
        uploadFileToHdfs(fileName);
        Double sizeKB = (double) fileSize / 1024;
        userService.saveUploadedFiles(user, fileName, sizeKB);
    }

    public List<Map> getFileInfoFromHdfs(String fileName) {
        String sql = "!hdfs -ls -F /tmp/upload/'''" + fileName + "''';";
        String result = engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
        return JacksonUtils.readJsonArray(result, Map.class);
    }

    public void deleteHdfsFile(String fileName) {
        String sql = "!hdfs -rm -r /tmp/upload/'''" + fileName + "''';";
        engineService.runScript(
                new EngineService.RunScriptParams()
                        .withSql(sql)
                        .withAsync("false"));
    }

    public void deleteHdfsFile(String user, String fileName) {
        deleteHdfsFile(fileName);
        userService.removeUploadedFiles(user, fileName);
    }

    public void deleteLocalFile(String userName, String fileName) {
        String filePath = System.getProperty("NOTEBOOK_HOME") + "/tmp/" + userName + "/" + fileName;
        File file = new File(filePath);
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            log.error("local file {} not exist", fileName);
        }
    }

    public void checkFileSize(String user, String fileName, long fileSize) {
        Double sizeKB = (double) fileSize / 1024;
        if (config.getUserFileSizeLimit() > 0 && sizeKB > config.getUserFileSizeLimit()) {
            throw new ByzerException(
                    ErrorCodeEnum.UPLOADED_FILE_TOO_LARGE,
                    config.getUserFileSizeLimit().intValue() / 1024
            );
        }
        UploadedFiles uploadedFiles = userService.getUploadedFileRecords(user);

        if (uploadedFiles == null || uploadedFiles.getTotalSize() == null || uploadedFiles.getFiles() == null) return;
        Double totalSize = uploadedFiles.getTotalSize();
        for (UploadedFiles.FileRecord f : uploadedFiles.getFiles()) {
            if (fileName.equals(f.getFileName())) {
                totalSize -= f.getFileSize();
            }
        }

        if (config.getUserTotalFileSizeLimit() > 0 && sizeKB + totalSize > config.getUserTotalFileSizeLimit()) {
            throw new ByzerException(
                    ErrorCodeEnum.UPLOADED_FILE_REACH_LIMIT,
                    config.getUserTotalFileSizeLimit().intValue() / 1024
            );
        }
    }

}
