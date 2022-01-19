package io.kyligence.notebook.console.service;

import io.kyligence.notebook.console.bean.dto.ExecFileDTO;
import io.kyligence.notebook.console.bean.entity.ExecFileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface FileInterface {

    ExecFileInfo create(String user, String name, Integer id);

    void checkResourceLimit(String user, Integer newResourceNum);

    void delete(Integer id);

    ExecFileInfo findById(Integer id);

    void checkExecFileAvailable(String user, ExecFileInfo execFileInfo, String commitId);

    ExecFileInfo find(String user, String name, Integer id);

    void updateById(ExecFileInfo execFileInfo);

    boolean isExecFileExist(String user, String name, Integer folderId);

    ExecFileDTO analyzeFile(MultipartFile file) throws IOException;

    ExecFileInfo importExecFile(ExecFileDTO execFileDTO, Integer valueOf);

    ExecFileDTO getFile(Integer id, String user);
}
