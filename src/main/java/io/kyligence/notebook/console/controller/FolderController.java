package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.ExecFileDTO;
import io.kyligence.notebook.console.bean.dto.IdNameDTO;
import io.kyligence.notebook.console.bean.dto.NotebookTreeDTO;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.bean.dto.req.CloneFolderReq;
import io.kyligence.notebook.console.bean.dto.req.CreateNotebookFolderReq;
import io.kyligence.notebook.console.bean.dto.req.MoveFolderReq;
import io.kyligence.notebook.console.bean.dto.req.RenameFolderReq;
import io.kyligence.notebook.console.bean.entity.NotebookFolder;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.FileInterface;
import io.kyligence.notebook.console.service.FolderService;
import io.kyligence.notebook.console.service.NotebookService;
import io.kyligence.notebook.console.service.WorkflowService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.EntityUtils;
import io.kyligence.notebook.console.util.FolderUtils;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowService workflowService;

    private FileInterface execFileService;

    @ApiOperation("Create Folder")
    @PostMapping("/folder")
    @Permission
    public Response<IdNameDTO> createFolder(@RequestBody @Validated CreateNotebookFolderReq createNotebookFolderReq) {
        String user = WebUtils.getCurrentLoginUser();
        String targetFolderId = createNotebookFolderReq.getTargetFolderId();
        String absPath = createNotebookFolderReq.getName();
        if (StringUtils.isNotBlank(targetFolderId)) {
            NotebookFolder targetFolder = folderService.findFolder(Integer.valueOf(targetFolderId));
            folderService.checkFolderAvailable(user, targetFolder);
            absPath = targetFolder.getAbsolutePath() + "." + absPath;
        }
        NotebookFolder notebookFolder = folderService.createFolder(user, absPath);
        IdNameDTO idNameDTO = IdNameDTO.valueOf(notebookFolder.getId(), notebookFolder.getName());
        return new Response<IdNameDTO>().data(idNameDTO);
    }

    @ApiOperation("Move Folder")
    @PostMapping("/folder/move")
    @Permission
    public Response moveFolder(@RequestBody @Validated MoveFolderReq moveFolderReq) {
        Integer currentFolderId = moveFolderReq.getCurrentFolderId();
        Integer targetFolderId = moveFolderReq.getTargetFolderId();
        String user = WebUtils.getCurrentLoginUser();
        String targetFolderPath = null;

        NotebookFolder currentFolder = folderService.findFolder(currentFolderId);
        folderService.checkFolderAvailable(user, currentFolder);

        NotebookFolder targetFolder;
        String newFolderAbsolutePath = currentFolder.getName();
        if (targetFolderId != null) {
            targetFolder = folderService.findFolder(targetFolderId);
            folderService.checkFolderAvailable(user, targetFolder);
            targetFolderPath = targetFolder.getAbsolutePath();
            newFolderAbsolutePath = targetFolderPath + "." + currentFolder.getName();
        }

        if (folderService.isFolderExist(user, newFolderAbsolutePath)) {
            throw new ByzerException(ErrorCodeEnum.SAME_SUB_FOLDER_NAME);
        }

        List<NotebookFolder> folders = folderService.findFolders(user);
        NotebookTreeDTO notebookTree = NotebookTreeDTO.valueOf(null, folders);
        notebookTree = notebookTree.subTree(EntityUtils.toStr(currentFolderId));
        String baseDir = FolderUtils.getParentFolder(currentFolder.getAbsolutePath());

        // move current dir
        currentFolder.setAbsolutePath(newFolderAbsolutePath);
        folderService.saveFolder(currentFolder);

        // move sub folders
        moveFolderRecursive(notebookTree, targetFolderPath, baseDir, true);

        return new Response();
    }

    private void moveFolderRecursive(NotebookTreeDTO notebookTree, String targetFolderPath, String baseDir, boolean root) {
        if (notebookTree == null) {
            return;
        }

        if (!root) {
            if (StringUtils.isNotBlank(notebookTree.getFolderId())) {
                NotebookFolder notebookFolder = folderService.findFolder(Integer.valueOf(notebookTree.getFolderId()));
                String relativePath = FolderUtils.getRelativePath(notebookFolder.getAbsolutePath(), baseDir);
                String newFolderPath = targetFolderPath == null ? relativePath : targetFolderPath + "." + relativePath;
                notebookFolder.setAbsolutePath(newFolderPath);
                folderService.saveFolder(notebookFolder);
            }
        }

        Optional.ofNullable(notebookTree.getList())
                .ifPresent(subFolders -> subFolders.forEach(subFolder -> moveFolderRecursive(subFolder, targetFolderPath, baseDir, false)));
    }

    @ApiOperation("Rename Folder")
    @PutMapping("/folder")
    @Permission
    public Response rename(@RequestBody @Validated RenameFolderReq renameFolderReq) {
        String user = WebUtils.getCurrentLoginUser();
        Integer folderId = renameFolderReq.getId();

        NotebookFolder notebookFolder = folderService.findFolder(folderId);
        folderService.checkFolderAvailable(user, notebookFolder);

        if (notebookFolder.getName().equals(renameFolderReq.getName())) {
            return new Response();
        }

        String originPath = notebookFolder.getAbsolutePath();
        String parentFolder = FolderUtils.getParentFolder(originPath);
        String newFolderPath = parentFolder == null ? renameFolderReq.getName() : parentFolder + "." + renameFolderReq.getName();
        if (folderService.isFolderExist(user, newFolderPath)) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_FOLDER_NAME);
        }

        NotebookTreeDTO notebookTreeDTO = folderService.getNotebookTree(user);
        NotebookTreeDTO folderTree = notebookTreeDTO.subTree(EntityUtils.toStr(folderId));
        renameFolderTree(folderTree, originPath, newFolderPath);

        return new Response();
    }

    @Transactional
    protected void renameFolderTree(NotebookTreeDTO folderTree, String originPath, String newFolderPath) {
        if (folderTree.folderId != null) {
            NotebookFolder notebookFolder = folderService.findFolder(Integer.valueOf(folderTree.folderId));
            if (notebookFolder.getAbsolutePath().equals(originPath)) {
                String newFolderName = FolderUtils.getFolderName(newFolderPath);
                notebookFolder.setName(newFolderName);
                notebookFolder.setAbsolutePath(newFolderPath);
            } else {
                String newAbsPath = notebookFolder.getAbsolutePath().replaceFirst(originPath, newFolderPath);
                notebookFolder.setAbsolutePath(newAbsPath);
            }
            folderService.saveFolder(notebookFolder);

            if (folderTree.list != null) {
                folderTree.list.forEach(subTree -> renameFolderTree(subTree, originPath, newFolderPath));
            }
        }
    }

    @ApiOperation("Clone Folder")
    @PostMapping("/folder/clone")
    @Permission
    public Response<IdNameDTO> cloneFolder(@RequestBody @Validated CloneFolderReq cloneFolderReq) {
        Integer folderId = cloneFolderReq.getId();
        String cloneFolderName = cloneFolderReq.getName();
        String user = WebUtils.getCurrentLoginUser();

        NotebookFolder notebookFolder = folderService.findFolder(folderId);
        folderService.checkFolderAvailable(user, notebookFolder);

        if (notebookFolder.getName().equals(cloneFolderReq.getName())) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_FOLDER_NAME);
        }

        String parentFolder = FolderUtils.getParentFolder(notebookFolder.getAbsolutePath());
        String newFolderPath = parentFolder == null ? cloneFolderName : parentFolder + "." + cloneFolderName;
        if (folderService.isFolderExist(user, newFolderPath)) {
            throw new ByzerException(ErrorCodeEnum.DUPLICATE_FOLDER_NAME);
        }

        NotebookTreeDTO notebookTreeDTO = folderService.getNotebookTree(user);
        NotebookTreeDTO folderTree = notebookTreeDTO.subTree(EntityUtils.toStr(folderId));

        List<NotebookFolder> root = new ArrayList<>();

        cloneFolderTree(user, folderTree, notebookFolder.getAbsolutePath(), newFolderPath, null, root);

        IdNameDTO idNameDTO = IdNameDTO.valueOf(root.get(0).getId(), root.get(0).getName());
        return new Response<IdNameDTO>().data(idNameDTO);
    }

    @Transactional
    protected void cloneFolderTree(String user, NotebookTreeDTO notebookTreeDTO, String originPath, String newPath, Integer folderId, List<NotebookFolder> root) {
        long currentTimestamp = System.currentTimeMillis();

        // clone notebook folder
        if (notebookTreeDTO.folderId != null) {
            NotebookFolder notebookFolder = folderService.findFolder(Integer.valueOf(notebookTreeDTO.folderId));
            NotebookFolder newFolder = new NotebookFolder();
            newFolder.setCreateTime(new Timestamp(currentTimestamp));
            newFolder.setUpdateTime(new Timestamp(currentTimestamp));
            newFolder.setUser(user);

            if (originPath.equals(notebookFolder.getAbsolutePath())) {
                // clone root folder
                String folderName = FolderUtils.getFolderName(newPath);
                newFolder.setName(folderName);
                newFolder.setAbsolutePath(newPath);
                newFolder = folderService.saveFolder(newFolder);
                root.add(newFolder);
            } else {
                // sub folder
                newFolder.setName(notebookFolder.getName());
                String newAbsPath = notebookFolder.getAbsolutePath().replaceFirst(originPath, newPath);
                newFolder.setAbsolutePath(newAbsPath);
                newFolder = folderService.saveFolder(newFolder);
            }

            Integer newFolderId = newFolder.getId();
            // clone notebooks and sub-folders in folder
            if (notebookTreeDTO.list != null) {
                notebookTreeDTO.list.forEach(subTree -> cloneFolderTree(user, subTree, originPath, newPath, newFolderId, null));
            }
            return;
        }

        // clone file
        if (notebookTreeDTO.id != null && folderId != null) {
            execFileService = getService(notebookTreeDTO.type);
            ExecFileDTO execFileDTO = execFileService.getFile(Integer.valueOf(notebookTreeDTO.id), user);
            execFileDTO.setName(execFileDTO.getName() + "_clone");
            execFileService.importExecFile(execFileDTO, folderId);
        }
    }

    private FileInterface getService(String type) {
        if (type.equals("workflow")) {
            execFileService = workflowService;
        } else if (type.equals("notebook")) {
            execFileService = notebookService;
        } else {
            throw new ByzerException(ErrorCodeEnum.NO_SUCH_TYPE);
        }
        return execFileService;
    }

    @ApiOperation("Delete Folder")
    @DeleteMapping("/folder/{id}")
    @Permission
    public Response deleteFolder(@PathVariable("id") @NotNull Integer folderId) {
        String user = WebUtils.getCurrentLoginUser();

        NotebookFolder notebookFolder = folderService.findFolder(folderId);
        folderService.checkFolderAvailable(user, notebookFolder);

        NotebookTreeDTO notebookTreeDTO = folderService.getNotebookTree(user);
        NotebookTreeDTO folderTree = notebookTreeDTO.subTree(EntityUtils.toStr(folderId));

        deleteFolderTree(folderTree);

        return new Response();
    }

    @Transactional
    protected void deleteFolderTree(NotebookTreeDTO folderTree) {
        if (folderTree.folderId != null) {
            folderService.deleteFolder(Integer.valueOf(folderTree.folderId));

            if (folderTree.list != null) {
                folderTree.list.forEach(this::deleteFolderTree);
            }
        }

        if (folderTree.id != null) {
            notebookService.delete(Integer.valueOf(folderTree.id));
        }

    }

}
