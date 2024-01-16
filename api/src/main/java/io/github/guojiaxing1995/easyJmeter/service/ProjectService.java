package io.github.guojiaxing1995.easyJmeter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.dto.project.CreateOrUpdateProjectDTO;
import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;

import java.util.List;


public interface ProjectService {

    IPage<ProjectDO> getProjectByName(Integer current, String name);

    boolean createProject(CreateOrUpdateProjectDTO validator);

    boolean updateProject(ProjectDO projectDO, CreateOrUpdateProjectDTO validator);

    ProjectDO getById(Integer id);

    boolean deleteProject(Integer id);

    List<ProjectDO> getAll();

    Integer getCaseCount(Integer id);
}
