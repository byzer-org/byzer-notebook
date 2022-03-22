package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.dto.req.CloneExecFileReq;
import io.kyligence.notebook.console.bean.dto.req.CreateNotebookReq;
import io.kyligence.notebook.console.bean.dto.req.MoveExecFileReq;
import io.kyligence.notebook.console.bean.dto.req.RenameExecFileReq;
import io.kyligence.notebook.console.bean.entity.ExecFileInfo;
import io.kyligence.notebook.console.bean.entity.NotebookFolder;
import io.kyligence.notebook.console.bean.entity.UserAction;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.*;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.JacksonUtils;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on file")
public class FileController {

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private UserService userService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private FolderService folderService;

    private FileInterface execFileService;

    @ApiOperation("Create file")
    @PostMapping("/file")
    @Permission
    public Response<IdDTO> create(@RequestBody @Validated CreateNotebookReq createNotebookReq) {
        String user = WebUtils.getCurrentLoginUser();
        String type = createNotebookReq.getType();
        execFileService = getService(type);
        execFileService.checkResourceLimit(user, 1);
        ExecFileInfo execFileInfo = execFileService.create(user, createNotebookReq.getName(), createNotebookReq.getFolderId());
        return new Response<IdDTO>().data(IdDTO.valueOf(execFileInfo.getId()));
    }

    @ApiOperation("Move File")
    @PostMapping("/file/move")
    @Permission
    @Transactional
    public Response moveExecFile(@RequestBody @Validated MoveExecFileReq moveExecFileReq) {
        Integer execFileId = moveExecFileReq.getId();
        Integer folderId = moveExecFileReq.getFolderId();
        String type = moveExecFileReq.getType();
        String user = WebUtils.getCurrentLoginUser();

        execFileService = getService(type);

        ExecFileInfo execFileInfo = execFileService.findById(execFileId);
        execFileService.checkExecFileAvailable(user, execFileInfo, null);

        // delete duplicate notebook in target folder
        ExecFileInfo origin = execFileService.find(user, execFileInfo.getName(), folderId);
        if (origin != null && !Objects.equals(origin.getId(), execFileInfo.getId())) {
            UserAction userAction = userService.getUserAction(user);
            List<OpenedExecFileDTO> openedNotebooks = JacksonUtils.readJsonArray(userAction.getOpenedNotebooks(), OpenedExecFileDTO.class);
            if (openedNotebooks != null && openedNotebooks.size() > 0) {
                List<OpenedExecFileDTO> newOpenedNotebooks = openedNotebooks.stream()
                        .filter(openedNotebook -> openedNotebook.getId().equals(origin.getId().toString()))
                        .collect(Collectors.toList());
                userService.saveOpenedExecfiles(user, JacksonUtils.writeJson(newOpenedNotebooks));
            }
            execFileService.delete(origin.getId());

        }

        if (folderId == null) {
            execFileInfo.setFolderId(null);
        }

        if (folderId != null) {
            NotebookFolder notebookFolder = folderService.findFolder(folderId);
            folderService.checkFolderAvailable(user, notebookFolder);

            execFileInfo.setFolderId(notebookFolder.getId());
        }

        execFileService.updateById(execFileInfo);
        return new Response();
    }

    @ApiOperation("Rename File")
    @PutMapping("/file")
    @Permission
    public Response<IdDTO> rename(@RequestBody @Validated RenameExecFileReq renameExecFileReq) {
        Integer execFileId = Integer.valueOf(renameExecFileReq.getId());
        String user = WebUtils.getCurrentLoginUser();
        String type = renameExecFileReq.getType();

        execFileService = getService(type);
        ExecFileInfo execFileInfo = execFileService.findById(execFileId);
        execFileService.checkExecFileAvailable(user, execFileInfo, null);

        String name = execFileInfo.getName();
        String rename = renameExecFileReq.getName();
        if (!rename.equals(name)) {
            if (execFileService.isExecFileExist(user, renameExecFileReq.getName(), execFileInfo.getFolderId())) {
                throw new ByzerException(ErrorCodeEnum.FILE_ALREADY_EXIST);
            }
        }
        execFileInfo.setName(renameExecFileReq.getName());
        execFileService.updateById(execFileInfo);

        return new Response<IdDTO>().data(IdDTO.valueOf(execFileId));
    }

    @ApiOperation("Clone File")
    @PostMapping("/file/clone")
    @Permission
    public Response<IdDTO> clone(@RequestBody @Validated CloneExecFileReq cloneExecFileReq) {
        Integer id = Integer.valueOf(cloneExecFileReq.getId());
        String type = cloneExecFileReq.getType();
        String user = WebUtils.getCurrentLoginUser();
        String commitId = cloneExecFileReq.getCommitId();

        execFileService = getService(type);
        execFileService.checkResourceLimit(user, 1);
        ExecFileInfo execFileInfo = execFileService.findById(id);
        execFileService.checkExecFileAvailable(user, execFileInfo, commitId);

        if (execFileService.isExecFileExist(user, cloneExecFileReq.getName(), execFileInfo.getFolderId())) {
            throw new ByzerException(ErrorCodeEnum.FILE_ALREADY_EXIST);
        }

        ExecFileDTO execFile = getExecFile(id, user, type, commitId);
        execFile.setName(cloneExecFileReq.getName());
        ExecFileInfo newExecFile = execFileService.importExecFile(execFile, execFileInfo.getFolderId());

        return new Response<IdDTO>().data(IdDTO.valueOf(newExecFile.getId()));
    }

    @ApiOperation("Delete File")
    @DeleteMapping("/file/{id}")
    @Permission
    public Response<IdDTO> delete(@PathVariable("id") @NotNull Integer id, @RequestParam("type") String type) {
        execFileService = getService(type);
        String user = WebUtils.getCurrentLoginUser();
        ExecFileInfo execFileInfo = execFileService.findById(id);
        execFileService.checkExecFileAvailable(user, execFileInfo, null);

        execFileService.delete(id);
        return new Response<IdDTO>().data(IdDTO.valueOf(id));
    }

    @ApiOperation("Get File List")
    @GetMapping("/files")
    @Permission
    public Response<NotebookTreeDTO> getNotebookList() {
        String user = WebUtils.getCurrentLoginUser();
        NotebookTreeDTO notebookTreeDTO = folderService.getNotebookTree(user);

        return new Response<NotebookTreeDTO>().data(notebookTreeDTO);
    }

    @ApiOperation("Import File")
    @PostMapping("/file/import")
    @SneakyThrows
    @Permission
    public Response<List<IdNameTypeDTO>> importExecFiles(@RequestParam("file") MultipartFile[] files) {
        checkFiles(files);
        List<IdNameTypeDTO> result = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                IdNameTypeDTO DTO = importExecFile(file);
                result.add(DTO);
            }
        }
        return new Response<List<IdNameTypeDTO>>().data(result);
    }

    private void checkFiles(MultipartFile[] files) throws IOException {
        String user = WebUtils.getCurrentLoginUser();
        List<IdNameTypeDTO> fileNameWithTypeList = new ArrayList<>();
        Map<String, Integer> fileNumMap = new HashMap<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fileType = getExecFileType(fileName);
            fileNumMap.put(fileType, fileNumMap.getOrDefault(fileType, 0) + 1);
            execFileService = getService(fileType);
            ExecFileDTO execFileDTO = execFileService.analyzeFile(file);

            if (execFileDTO == null) {
                throw new ByzerException(ErrorCodeEnum.FILE_FORMAT_MISMATCH);
            }
            String insideFilename = execFileDTO.getName();
            if (insideFilename == null || !insideFilename.matches("[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
                throw new ByzerException(ErrorCodeEnum.FILE_FORMAT_MISMATCH, fileType);
            }
            if (fileNameWithTypeList.contains(IdNameTypeDTO.valueOf(null, insideFilename, fileType))) {
                throw new ByzerException(ErrorCodeEnum.DUPLICATE_FOLDER_NAME);
            }

            fileNameWithTypeList.add(IdNameTypeDTO.valueOf(null, insideFilename, fileType));
        }
        fileNumMap.forEach((fileType, num) ->{
            execFileService = getService(fileType);
            execFileService.checkResourceLimit(user, num);
        });
    }

    private IdNameTypeDTO importExecFile(MultipartFile file) throws IOException {
        String type = getExecFileType(file.getOriginalFilename());
        execFileService = getService(type);
        ExecFileDTO execFileDTO = execFileService.analyzeFile(file);
        ExecFileInfo execFileInfo = execFileService.importExecFile(execFileDTO, null);
        return IdNameTypeDTO.valueOf(execFileInfo.getId(), execFileInfo.getName(), type);
    }

    @ApiOperation("Export File")
    @GetMapping("/file/export/{id}")
    @Permission
    @SneakyThrows
    public void exportNotebook(@PathVariable("id") Integer execFileId, @RequestParam("type") String type,
                               @RequestParam(value = "commit_id", required = false) String commitId,
                               @RequestParam(value = "output", required = false, defaultValue = "json") String outputType,
                               HttpServletResponse response) {
        String user = WebUtils.getCurrentLoginUser();

        ExecFileDTO execFileDTO = getExecFile(execFileId, user, type, commitId);

        String fileName = String.format(Locale.ROOT, "%s_%s", execFileDTO.getName(),
                new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault(Locale.Category.FORMAT)).format(new Date()));

        String content;
        response.setContentType("application/xml;charset=UTF-8");

        if (outputType.equalsIgnoreCase("byzer")) {
            content = getExecFileContent(user, execFileId, type, commitId);
            response.setHeader("Content-Disposition",
                    String.format(Locale.ROOT, "attachment; filename=\"%s.byzer\"", fileName));
        } else {
            String extName = getExtension(type);
            response.setHeader("Content-Disposition",
                    String.format(Locale.ROOT, "attachment; filename=\"%s.%s\"", fileName, extName));
            content = JacksonUtils.writeJson(execFileDTO);
        }
        assert content != null;

        response.getWriter().write(content);
        response.getWriter().flush();
        response.getWriter().close();
    }

    @ApiOperation("Get User Opened File")
    @GetMapping("/files/opened")
    @Permission
    public Response<OpenedExecFileListDTO> getOpenedNotebooks() {
        String user = WebUtils.getCurrentLoginUser();
        UserAction userAction = userService.getUserAction(user);

        return new Response<OpenedExecFileListDTO>().data(OpenedExecFileListDTO.valueOf(userAction));
    }

    @ApiOperation("Save User Opened File")
    @PutMapping("/files/opened")
    @Permission
    public Response saveOpenedNotebooks(@RequestBody @Validated OpenedExecFileListDTO openedNotebooks) {
        String user = WebUtils.getCurrentLoginUser();
        String openedNotebooksContent = null;

        if (openedNotebooks != null && openedNotebooks.getList() != null) {
            openedNotebooksContent = JacksonUtils.writeJson(openedNotebooks.getList());
        }

        userService.saveOpenedExecfiles(user, openedNotebooksContent);

        return new Response();
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

    private String getExecFileType(String filename) {
        String type;
        if (filename.endsWith("mlnb") || filename.endsWith("bznb")) {
            type = "notebook";
        } else if (filename.endsWith("mlwf") || filename.endsWith("bzwf")) {
            type = "workflow";
        } else {
            throw new ByzerException(ErrorCodeEnum.UNSUPPORTED_EXT_NAME);
        }
        return type;
    }

    private String getExtension(String type) {
        if (type.equals("notebook")) {
            return "bznb";
        } else if (type.equals("workflow")) {
            return "bzwf";
        } else {
            throw new ByzerException(ErrorCodeEnum.NO_SUCH_TYPE);
        }
    }

    private ExecFileDTO getExecFile(Integer execFileId, String user, String type, String commitId) {
        if (type.equals("notebook")) {
            return notebookService.getNotebook(execFileId, user, commitId);
        } else if (type.equals("workflow")) {
            return workflowService.getWorkflow(execFileId, user, commitId);
        } else {
            throw new ByzerException(ErrorCodeEnum.NO_SUCH_TYPE);
        }
    }

    private String getExecFileContent(String user, Integer execFileId, String type, String commitId) {
        if (type.equals("notebook")) {
            return notebookService.getNotebookScripts(user, execFileId, commitId,
                    new EngineService.RunScriptParams().getAll());
        } else if (type.equals("workflow")) {
            return workflowService.getWorkflowScripts(user, execFileId, commitId,
                    new EngineService.RunScriptParams().getAll());
        } else {
            throw new ByzerException(ErrorCodeEnum.NO_SUCH_TYPE);
        }
    }

}
