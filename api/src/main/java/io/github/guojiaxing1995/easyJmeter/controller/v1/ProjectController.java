package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.dto.project.CreateOrUpdateProjectDTO;
import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;
import io.github.guojiaxing1995.easyJmeter.service.ProjectService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import io.github.guojiaxing1995.easyJmeter.vo.DeletedVO;
import io.github.guojiaxing1995.easyJmeter.vo.UpdatedVO;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
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
    @PostMapping("")
    @ApiOperation(value = "项目创建", notes = "输入名称、描述创建项目")
    @LoginRequired
    public CreatedVO creatProject(@RequestBody @Validated CreateOrUpdateProjectDTO validator){
        projectService.createProject(validator);
        return new CreatedVO(1);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "项目更新", notes = "输入名称、描述更新项目")
    @LoginRequired
    public UpdatedVO updateProject(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, @RequestBody @Validated CreateOrUpdateProjectDTO validator){
        ProjectDO project = projectService.getById(id);
        if (project == null){
            throw new NotFoundException(10022);
        }
        projectService.updateProject(project, validator);
        return new UpdatedVO(2);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "项目删除", notes = "删除指定项目")
    @LoginRequired
    public DeletedVO deleteProject(@PathVariable("id") @Positive(message = "{id.positive}") Integer id){
        ProjectDO project = projectService.getById(id);
        if (project == null){
            throw new NotFoundException(10022);
        }
        projectService.deleteProject(id);
        return new DeletedVO(3);
    }
}
