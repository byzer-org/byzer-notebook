package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.bean.dto.NotebookTreeDTO;
import io.kyligence.notebook.console.bean.entity.*;
import io.kyligence.notebook.console.controller.NotebookHelper;
import io.kyligence.notebook.console.dao.SharedFileRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileShareService {
    @Autowired
    private SharedFileRepository sharedFileRepository;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private NotebookHelper notebookHelper;

    @Autowired
    private WorkflowService workflowService;

    private NotebookTreeDTO cachedDemo;

    private final String ADMIN_ACCOUNT = "admin";

    @PostConstruct
    public void createDefaultDemo(){
        // create default sample for admin
        if (Objects.isNull(notebookService.getDefault(ADMIN_ACCOUNT))) {
            notebookHelper.createSampleDemo(ADMIN_ACCOUNT);
        }

        // create default demo for all user from admin default sample
        if (Objects.isNull(notebookService.getDefaultDemo())){
            notebookService.find(ADMIN_ACCOUNT).stream()
                    .filter(nb -> nb.getType().equalsIgnoreCase("default"))
                    .forEach(nb -> addNotebookDemo(ADMIN_ACCOUNT, nb.getId()));
            workflowService.find(ADMIN_ACCOUNT).forEach(wf -> addWorkflowDemo(ADMIN_ACCOUNT, wf.getId()));
        }
    }

    public void addDemo(String user, Integer entityId, String entityType) {
        if (!user.equalsIgnoreCase(ADMIN_ACCOUNT)) {
            throw new ByzerException("Not Authorized");
        }
        if (entityType.equalsIgnoreCase("notebook")) {
            addNotebookDemo(user, entityId);
        } else if (entityType.equalsIgnoreCase("workflow")) {
            addWorkflowDemo(user, entityId);
        } else {
            throw new ByzerException("Unsupported Entity Type: [" + entityType + "]");
        }
        // clear cache, reload in next transaction
        clearCachedDemo();
    }

    public void deleteDemo(String user, Integer entityId, String entityType) {
        if (!user.equalsIgnoreCase(ADMIN_ACCOUNT)) {
            throw new ByzerException("Not Authorized");
        }
        sharedFileRepository.deleteByEntity(user, entityId, entityType);
        loadDemo();
    }

    private SharedFileInfo findByEntity(String user, Integer entityId, String entityType){
        List<SharedFileInfo> infos = sharedFileRepository.findByEntity(user, entityId, entityType);
        return infos.isEmpty() ? null : infos.get(0);
    }

    private void addWorkflowDemo(String user, Integer workflowId) {
        WorkflowCommit commit = workflowService.commit(user, workflowId);
        SharedFileInfo sharedFileInfo = findByEntity(user, workflowId, "workflow");
        long timestamp = System.currentTimeMillis();

        if (Objects.isNull(sharedFileInfo)) {
            sharedFileInfo = new SharedFileInfo();
            sharedFileInfo.setEntityId(workflowId);
            sharedFileInfo.setEntityType("workflow");
            sharedFileInfo.setCommitId(commit.getCommitId());
            sharedFileInfo.setOwner(user);
            sharedFileInfo.setCreateTime(new Timestamp(timestamp));
            sharedFileInfo.setUpdateTime(new Timestamp(timestamp));
            sharedFileRepository.save(sharedFileInfo);
        } else {
            sharedFileRepository.updateCommit(user, workflowId, "workflow", commit.getCommitId());
        }
    }

    private void addNotebookDemo(String user, Integer notebookId) {
        NotebookCommit commit = notebookService.commit(user, notebookId);
        SharedFileInfo sharedFileInfo = findByEntity(user, notebookId, "notebook");
        long timestamp = System.currentTimeMillis();

        if (Objects.isNull(sharedFileInfo)) {
            sharedFileInfo = new SharedFileInfo();
            sharedFileInfo.setEntityId(notebookId);
            sharedFileInfo.setEntityType("notebook");
            sharedFileInfo.setCommitId(commit.getCommitId());
            sharedFileInfo.setOwner(user);
            sharedFileInfo.setCreateTime(new Timestamp(timestamp));
            sharedFileInfo.setUpdateTime(new Timestamp(timestamp));
            sharedFileRepository.save(sharedFileInfo);
        } else {
            sharedFileRepository.updateCommit(user, notebookId, "notebook", commit.getCommitId());
        }
    }

    public NotebookTreeDTO getDemo() {
        if (Objects.isNull(this.cachedDemo)) {
            this.loadDemo();
        }
        return this.cachedDemo;
    }

    private void loadDemo() {
        List<SharedFileInfo> demos = sharedFileRepository.findByOwner(ADMIN_ACCOUNT);
        List<NotebookCommit> demoNotebooks = Lists.newArrayList();
        List<WorkflowCommit> demoWorkflows = Lists.newArrayList();
        demos.forEach(
                demo -> {
                    if (demo.getEntityType().equalsIgnoreCase("notebook")) {
                        NotebookCommit nb = notebookService.findCommit(demo.getEntityId(), demo.getCommitId());
                        if (Objects.nonNull(nb)) demoNotebooks.add(nb);
                    } else if (demo.getEntityType().equalsIgnoreCase("workflow")) {
                        WorkflowCommit wf = workflowService.findCommit(demo.getEntityId(), demo.getCommitId());
                        if (Objects.nonNull(wf)) demoWorkflows.add(wf);
                    }
                }
        );
        this.cachedDemo = NotebookTreeDTO.valueOfDemoFiles(demoNotebooks, demoWorkflows);
    }

    /**
     * Clear Demo Cache.
     */
    private void clearCachedDemo(){
        this.cachedDemo = null;
    }
}
