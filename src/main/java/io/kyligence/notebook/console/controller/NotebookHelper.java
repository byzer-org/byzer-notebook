package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.CellInfoDTO;
import io.kyligence.notebook.console.bean.dto.NotebookDTO;
import io.kyligence.notebook.console.bean.dto.WorkflowDTO;
import io.kyligence.notebook.console.bean.entity.CellInfo;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.service.UploadFileService;
import io.kyligence.notebook.console.service.NotebookService;
import io.kyligence.notebook.console.service.WorkflowService;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class NotebookHelper {

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UploadFileService uploadFileService;

    @Transactional
    protected NotebookInfo importNotebook(NotebookDTO notebookDTO, Integer folderId, String type) {
        // create notebook
        String user = WebUtils.getCurrentLoginUser();
        long timestamp = System.currentTimeMillis();
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setUser(WebUtils.getCurrentLoginUser());

        if (notebookService.isNotebookExist(user, notebookDTO.getName(), folderId)) {
            String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault(Locale.Category.FORMAT)).format(new Date());
            notebookInfo.setName(notebookDTO.getName() + "_imported_" + time);
        } else {
            notebookInfo.setName(notebookDTO.getName());
        }

        notebookInfo.setCreateTime(new Timestamp(timestamp));
        notebookInfo.setUpdateTime(new Timestamp(timestamp));
        notebookInfo.setType(type);
        notebookInfo.setFolderId(folderId);

        notebookInfo = notebookService.save(notebookInfo);
        Integer notebookId = notebookInfo.getId();

        // 2. create notebook cells
        if (notebookDTO.getCellList() == null || notebookDTO.getCellList().isEmpty()) {
            return notebookInfo;
        }
        List<Integer> cellIds = new ArrayList<>();

        for (CellInfoDTO cellInfoDTO : notebookDTO.getCellList()) {
            CellInfo cellInfo = new CellInfo();
            cellInfo.setNotebookId(notebookId);
            cellInfo.setContent(cellInfoDTO.getContent());
            cellInfo.setCreateTime(new Timestamp(timestamp));
            cellInfo.setUpdateTime(new Timestamp(timestamp));
            cellInfo = notebookService.save(cellInfo);
            cellIds.add(cellInfo.getId());
        }

        // 3. update notebook cell ids
        String cellList = JacksonUtils.writeJson(cellIds);
        notebookInfo.setCellList(cellList);
        return notebookService.save(notebookInfo);
    }

    protected NotebookInfo importNotebook(NotebookDTO notebookDTO) {
        return importNotebook(notebookDTO, null, null);
    }

    public NotebookInfo importNotebook(NotebookDTO notebookDTO, Integer folderId) {
        return importNotebook(notebookDTO, folderId, null);
    }

    @SneakyThrows
    protected NotebookInfo createSampleDemo(String user) {
        log.info("Creating Demo For User:" + user);
        String username = user.toLowerCase();
        NotebookInfo result = null;

        String NOTEBOOK_HOME = System.getProperty("NOTEBOOK_HOME");
        File demoDir = new File(NOTEBOOK_HOME + "/sample");
        File[] demoFiles = demoDir.listFiles();
        for (File demo : demoFiles) {
            try (FileInputStream in = new FileInputStream(demo)) {
                log.info("Import File: " + demo.getName());
                if (demo.getName().endsWith(".mlnb") || demo.getName().endsWith(".bznb")) {
                    NotebookDTO notebookDTO = JacksonUtils.readJson(in, NotebookDTO.class);
                    NotebookInfo nb= importNotebook(notebookDTO, null, "default");
                    if (result == null) result = nb;
                } else if (demo.getName().endsWith(".mlwf") || demo.getName().endsWith(".bzwf")){
                    WorkflowDTO workflowDTO = JacksonUtils.readJson(in, WorkflowDTO.class);
                    workflowService.importWorkflow(workflowDTO, null, "default");
                }

            } catch (Exception e) {
                log.error("can not import default notebook.");
            }
        }
        // upload sample files
        File localUserTmpDir = new File(NOTEBOOK_HOME + "/tmp/" + username);
        if (!localUserTmpDir.exists()) {
            localUserTmpDir.mkdirs();
        }
        File sampleDir = new File(NOTEBOOK_HOME + "/sample/data");
        File destDir = new File(NOTEBOOK_HOME + "/tmp/" + username);
        File[] sampleFiles = sampleDir.listFiles();
        for (File sampleFile : sampleFiles) {
            FileUtils.copyFileToDirectory(sampleFile, destDir);
            uploadFileService.uploadFileToHdfs(sampleFile.getName());
        }
        log.info("Demo Created For User:" + user);
        return result;
    }
}
