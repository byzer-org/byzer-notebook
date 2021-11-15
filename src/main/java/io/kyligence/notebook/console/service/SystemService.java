package io.kyligence.notebook.console.service;


import io.kyligence.notebook.console.NotebookConfig;
import io.kyligence.notebook.console.bean.entity.SystemConfig;
import io.kyligence.notebook.console.dao.SystemConfigRepository;
import io.kyligence.notebook.console.support.EncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
}
