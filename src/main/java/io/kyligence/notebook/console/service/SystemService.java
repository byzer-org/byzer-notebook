package io.kyligence.notebook.console.service;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.VersionInfo;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.dao.SystemConfigRepository;
import io.kyligence.notebook.console.support.EncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;


@Service
public class SystemService {

    private NotebookConfig notebookConfig = NotebookConfig.getInstance();

    @Autowired
    private SystemConfigRepository repository;

    private boolean initFlag = false;

    public void updateConfig(SystemConfig systemConfig) {
        repository.save(systemConfig);

        if (systemConfig.getTimeout() != null) {
            notebookConfig.updateConfig("notebook.execution.timeout", String.valueOf(systemConfig.getTimeout()));
        }

        if (systemConfig.getEngine() != null) {
            notebookConfig.updateConfig("notebook.execution.engine", systemConfig.getEngine());
        }

    }


    public SystemConfig getConfig() {
        SystemConfig systemConfig = repository.findAll().get(0);
        if (!initFlag) {
            updateConfig(systemConfig);
            initFlag = true;
        }
        return systemConfig;
    }

    public String getCipherKey(){
        return EncryptUtils.getKey();
    }

    public VersionInfo getVersionInfo(){
        String version = readFile(notebookConfig.getVersionPath());
        long buildTime = getBuildTime(notebookConfig.getVersionPath());
        String backendCommitSHA = readFile(notebookConfig.getCommitSHAPath());
        String frontendCommitSHA = readFile(notebookConfig.getFrontendCommitSHAPath());
        return VersionInfo.valueOf(buildTime, frontendCommitSHA, backendCommitSHA, version);
    }

    private String readFile(String filePath) {
        String encoding = "UTF-8";
        File file = new File(filePath);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(fileContent);
            return new String(fileContent, encoding).trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private long getBuildTime(String filePath){
        File file = new File(filePath);
        Path path = file.toPath();
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            return attr.creationTime().toMillis();
        } catch (Exception e) {
            return file.lastModified();
        }
    }
}
