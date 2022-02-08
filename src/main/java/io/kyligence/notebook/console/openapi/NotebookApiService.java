package io.kyligence.notebook.console.openapi;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.kyligence.notebook.console.bean.dto.NotebookDTO;
import io.kyligence.notebook.console.bean.dto.Response;
import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import io.kyligence.notebook.console.service.NotebookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import liquibase.pro.packaged.S;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("api/service/")
@Api("The documentation about notebook open api.")
public class NotebookApiService {

    @Autowired
    private NotebookService notebookService;

    @ApiOperation("Get Notebook Content")
    @GetMapping("/notebook/{id}")
    public Response<NotebookDTO> getNotebook(HttpServletRequest request, @PathVariable("id") @NotNull Integer id) {
        String authorization = request.getHeader("Authorization");
        String token = StringUtils.substringAfter(authorization, "bearer ");
        log.info("【NotebookApiService】 /api/service/getNotebook/{} authentication={}", id, authorization);
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey("37140dc5-63af-45ee-8e07-bf6b438767f8")
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {
            throw new ByzerException(ErrorCodeEnum.Forbidden, e.getMessage());
        }
        if (claims != null && claims.containsKey("name")) {
            String name = (String) claims.get("name");
            log.info("【NotebookApiService】 getNotebook name={}", name);
            if ("admin".equals(name)) {
                NotebookDTO notebookDTO = notebookService.getNotebook(id, "admin");
                return new Response<NotebookDTO>().data(notebookDTO);
            }
        }
        throw new ByzerException(ErrorCodeEnum.Forbidden, "【NotebookApiService】The payload is illegal!");


    }
}
