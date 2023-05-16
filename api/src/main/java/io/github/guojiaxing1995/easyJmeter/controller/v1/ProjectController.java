package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;
import io.github.guojiaxing1995.easyJmeter.service.ProjectService;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/project")
@Api(tags = "项目管理")
@Validated
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("")
    @ApiOperation(value = "项目查询", notes = "根据项目名称查询项目")
    @LoginRequired
    public List<ProjectDO> search(@RequestParam(value = "name", required = false, defaultValue = "") String name){
        List<ProjectDO> projects = projectService.getProjectByName(name);
        return projects;
    }
}
