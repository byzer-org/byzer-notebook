package io.kyligence.notebook.console.service;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.dto.VersionInfo;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.dao.SystemConfigRepository;
import io.kyligence.notebook.console.support.CriteriaQueryBuilder;
import io.kyligence.notebook.console.support.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.Query;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;


@Slf4j
@Service
public class SystemService {

    private final NotebookConfig notebookConfig = NotebookConfig.getInstance();

    @Autowired
    private SystemConfigRepository repository;

    @Resource
    private CriteriaQueryBuilder queryBuilder;

    public boolean isMetaDBReachable() {
        // test meta database reachable
        try {
            repository.findAll();
            return true;
        } catch (Exception ex) {
            log.error("Can not access meta database table, please check database status!");
            return false;
        }
    }

    @Transactional
    public void updateByUser(SystemConfig systemConfig) {
        Query query = queryBuilder.updateNotNullByField(systemConfig, "user");
        query.executeUpdate();
    }

    public SystemConfig getConfig(String user) {
        List<SystemConfig> systemConfigs = repository.findByUser(user);
        if (systemConfigs.isEmpty()) {
            SystemConfig systemConfig =  new SystemConfig();
            systemConfig.setUser(user);
            systemConfig.setEngine(notebookConfig.getExecutionEngine());
            systemConfig.setTimeout(notebookConfig.getExecutionTimeout());
            return repository.save(systemConfig);
        }
        return systemConfigs.get(0);
    }

    public String getCipherKey() {
        return EncryptUtils.getKey();
    }

    public VersionInfo getVersionInfo() {
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

    private long getBuildTime(String filePath) {
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
