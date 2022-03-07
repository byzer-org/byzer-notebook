package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.bean.dto.NotebookTreeDTO;
import io.kyligence.notebook.console.bean.entity.ExecFileInfo;
import io.kyligence.notebook.console.bean.entity.NotebookFolder;
import io.kyligence.notebook.console.bean.entity.NotebookInfo;
import io.kyligence.notebook.console.bean.entity.WorkflowInfo;
import io.kyligence.notebook.console.dao.NotebookFolderRepository;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
public class FolderService {

    @Autowired
    private NotebookFolderRepository folderRepository;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private FileShareService fileShareService;

    @Transactional
    public NotebookFolder createFolder(String user, String folderPath) {
        if (isFolderExist(user, folderPath)) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_FOLDER_NAME);
        }

        String[] folderParts = folderPath.split("[.]");

        long currentTimeStamp = System.currentTimeMillis();
        NotebookFolder notebookFolder = new NotebookFolder();
        notebookFolder.setName(folderParts[folderParts.length - 1]);
        notebookFolder.setUser(user);
        notebookFolder.setAbsolutePath(folderPath);
        notebookFolder.setCreateTime(new Timestamp(currentTimeStamp));
        notebookFolder.setUpdateTime(new Timestamp(currentTimeStamp));

        return folderRepository.save(notebookFolder);
    }

    public NotebookFolder saveFolder(NotebookFolder notebookFolder) {
        return folderRepository.save(notebookFolder);
    }

    public boolean isFolderExist(String user, String folder) {
        List<NotebookFolder> folders = folderRepository.findByAbsolutePath(user, folder);
        return folders != null && folders.size() > 0 && folders.get(0).getAbsolutePath().equals(folder);
    }

    public NotebookFolder findFolder(Integer folderId) {
        return folderRepository.findById(folderId).orElse(null);
    }

    public NotebookFolder findFolder(String user, String absPath) {
        List<NotebookFolder> folders = folderRepository.findByAbsolutePath(user, absPath);
        if (folders == null || folders.size() == 0) {
            return null;
        }
        return folders.get(0);
    }

    public void deleteFolder(Integer id) {
        folderRepository.deleteById(id);
    }

    public List<NotebookFolder> findFolders(String user) {
        return folderRepository.find(user);
    }

    public void checkFolderAvailable(String user, NotebookFolder notebookFolder) {
        if (notebookFolder == null) {
            throw new ByzerException(ErrorCodeEnum.FOLDER_NOT_EXIST);
        }
        if (!user.equalsIgnoreCase(notebookFolder.getUser())) {
            throw new ByzerException(ErrorCodeEnum.FOLDER_NOT_AVAILABLE);
        }
    }

    public  NotebookTreeDTO getNotebookTree(String user) {
        List<NotebookInfo> notebooks = notebookService.find(user);
        List<WorkflowInfo> workflows = workflowService.find(user);
        List<NotebookFolder> folders = this.findFolders(user);
        List<ExecFileInfo> execFiles = ExecFileInfo.createArrayFiles(notebooks, workflows);

        // if trans here, can not figure out type: notebook/workflow
        NotebookTreeDTO userFiles = NotebookTreeDTO.valueOf(execFiles, folders);

        NotebookTreeDTO demoFolder = fileShareService.getDemo();
        if (!demoFolder.getList().isEmpty()) userFiles.getList().add(0, demoFolder);
        return userFiles;
    }

}
