package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.FileInfoDTO;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.UploadFileService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("api")
@Api("The documentation about operations on file")
public class UploadFileController {

    @Autowired
    private UploadFileService uploadFileService;

    private static final String NOTEBOOK_HOME = System.getProperty("NOTEBOOK_HOME");

    @ApiOperation("upload file")
    @PostMapping(value = "/upload_file")
    @Permission
    public Response uploadFiles(@RequestParam("file") MultipartFile[] files) {
        if (files != null) {
            for (MultipartFile file : files) {
                uploadFile(file);
            }
        }
        return new Response();
    }

    @SneakyThrows
    private void uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ByzerException(ErrorCodeEnum.EMPTY_FILE);
        }

        String user = WebUtils.getCurrentLoginUser();
        uploadFileService.checkFileSize(user, file.getOriginalFilename(), file.getSize());

        File localUserTmpDir = new File(NOTEBOOK_HOME + "/tmp/" + user);
        if (!localUserTmpDir.exists()) {
            localUserTmpDir.mkdirs();
        }
        File dest = new File(localUserTmpDir, file.getOriginalFilename());

        file.transferTo(dest);
        String fileName = dest.getName();

        List<Map> result = uploadFileService.getFileInfoFromHdfs(fileName);
        if (result != null) {
            uploadFileService.deleteHdfsFile(user, fileName);
        }
        uploadFileService.uploadFileToHdfs(user, fileName, file.getSize());
    }


    @SneakyThrows
    @ApiOperation("download file - from mlsql")
    @GetMapping("/upload_file")
    public void download(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("userName").toLowerCase();
        String fileName = request.getParameter("fileName");

        String filePath = System.getProperty("NOTEBOOK_HOME") + "/tmp/" + username + "/" + fileName;
        File file = new File(filePath);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".tar\"");

        try (ArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(response.getOutputStream())) {
            log.info("User: [" + username + "] Downloading File: " + fileName);
            ArchiveEntry entry = tarOutputStream.createArchiveEntry(file, fileName);
            tarOutputStream.putArchiveEntry(entry);
            IOUtils.copyLarge(new FileInputStream((file)), tarOutputStream);
            tarOutputStream.closeArchiveEntry();
            tarOutputStream.flush();
        }

    }
    @Permission
    @SneakyThrows
    @ApiOperation("delete file - from mlsql")
    @DeleteMapping("/upload_file")
    public Response<FileInfoDTO> delete(@RequestParam(value = "file_name") String fileName) {
        String userName = WebUtils.getCurrentLoginUser();
        uploadFileService.deleteLocalFile(userName, fileName);
        List<Map> fileInfo = uploadFileService.getFileInfoFromHdfs(fileName);
        if (fileInfo == null) {
            throw new ByzerException(ErrorCodeEnum.FILE_NOT_EXIST);
        } else if (fileInfo.get(0).containsKey("_corrupt_record")) {
            String mlsqlException = String.valueOf(fileInfo.get(0).get("_corrupt_record"));
            throw new ByzerException(ErrorCodeEnum.MLSQL_EXCEPTION, mlsqlException);
        }
        uploadFileService.deleteHdfsFile(userName, fileName);
        return new Response<FileInfoDTO>().data(FileInfoDTO.valueOf(userName, fileName));
    }


}
