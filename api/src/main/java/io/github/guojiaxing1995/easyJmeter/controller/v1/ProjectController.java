package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.util.PageUtil;
import io.github.guojiaxing1995.easyJmeter.dto.project.CreateOrUpdateProjectDTO;
import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;
import io.github.guojiaxing1995.easyJmeter.service.ProjectService;
import io.github.guojiaxing1995.easyJmeter.service.UserService;
import io.github.guojiaxing1995.easyJmeter.vo.*;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.LoginRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/project")
@Api(tags = "项目管理")
@Validated
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    @ApiOperation(value = "项目查询", notes = "根据项目名称查询项目")
    @LoginRequired
    @GroupRequired
    @PermissionMeta(value = "项目管理", module = "项目")
    public PageResponseVO<ProjectInfoVO> searchProject(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                                            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page){
        IPage<ProjectDO> projects = projectService.getProjectByName(page, name);
        List<ProjectInfoVO> projectInfos = projects.getRecords().stream().map(project -> {
            String username = userService.getById(project.getCreator()).getUsername();
            Integer caseCount = projectService.getCaseCount(project.getId());
            return new ProjectInfoVO(project, username, caseCount);
        }).collect(Collectors.toList());
        return PageUtil.build(projects, projectInfos);
    }

    @GetMapping("/all")
    @ApiOperation(value = "项目列表", notes = "获取所有项目")
    @LoginRequired
    public List<ProjectDO> searchProject() {
        return projectService.getAll();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "项目查询-id", notes = "获取指定id的项目")
    @LoginRequired
    public ProjectDO getProject(@PathVariable("id") @Positive(message = "{id.positive}") Integer id){
        ProjectDO project = projectService.getById(id);
        if (project == null){
            throw new NotFoundException(10022);
        }
        return project;
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
