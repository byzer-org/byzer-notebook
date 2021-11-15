package io.kyligence.notebook.console.controller;

import io.kyligence.notebook.console.bean.dto.*;
import io.kyligence.notebook.console.bean.dto.req.CUNodeReq;
import io.kyligence.notebook.console.bean.dto.req.DynamicDependsReq;
import io.kyligence.notebook.console.bean.entity.NodeInfo;
import io.kyligence.notebook.console.service.ETService;
import io.kyligence.notebook.console.support.Permission;
import io.kyligence.notebook.console.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api")
@Api("The documentation about operations on ET node")
public class ETController {

    @Autowired
    private ETService etService;

//    @ApiOperation("get et dependency")
//    @PostMapping("/et/dependency")
//    @Permission
//    public Response<> getETDependency(@RequestBody ETDependReq etDependReq) {
//

    @ApiOperation("get all et nodes")
    @GetMapping("/et")
//    @Permission
    public Response<List<ETNodeDTO>> getETNodes() {
        List<ETNodeDTO> ETNodeDTOS = etService.getAllET();
        return new Response<List<ETNodeDTO>>().data(ETNodeDTOS);
    }

    @ApiOperation("get et params")
    @GetMapping("/et/{et_id}/params")
//    @Permission
    public Response<ETDetail> getETParams(@PathVariable("et_id") Integer ETId) {
        List<ETParamDTO> params = etService.loadETParams(ETId);
        String usage = etService.getETUsage(ETId);
        String name = etService.getETName(ETId);
        return new Response<ETDetail>().data(ETDetail.valueOf(name, usage, params));
    }

    @ApiOperation("Dynamic depends")
    @PostMapping("/et/dependency")
    @Permission
    public Response<List<ETParamDTO.ValueBehavior>> queryDependency(@RequestBody @Validated DynamicDependsReq dependsReq) {
        return new Response<List<ETParamDTO.ValueBehavior>>().data(etService.dynamicDepends(dependsReq));
    }

}
