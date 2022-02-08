package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.NotebookDTO;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.service.NotebookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("api/service/")
@Api("The documentation about notebook open api.")
public class OpenApi {

    @Autowired
    private NotebookService notebookService;

    @ApiOperation("Get Notebook Content")
    @GetMapping("/notebook/{id}")
    public Response<NotebookDTO> getNotebook(@PathVariable("id") @NotNull Integer id) {
        NotebookDTO notebookDTO = notebookService.getNotebook(id, "admin");
        return new Response<NotebookDTO>().data(notebookDTO);
    }
}
