package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.mybatis.Page;
import io.github.guojiaxing1995.easyJmeter.dto.project.CreateOrUpdateProjectDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.ProjectMapper;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;
import io.github.guojiaxing1995.easyJmeter.model.UserDO;
import io.github.guojiaxing1995.easyJmeter.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CaseMapper caseMapper;

    @Override
    public IPage<ProjectDO> getProjectByName(Integer current, String name) {
        Page page = new Page(current, 10);
        IPage<ProjectDO> projects = projectMapper.selectByName(page, name);
        return projects;
    }

    @Override
    public boolean createProject(CreateOrUpdateProjectDTO validator) {
        ProjectDO projectDO = new ProjectDO();
        projectDO.setName(validator.getName());
        UserDO user = LocalUser.getLocalUser();
        projectDO.setCreator(user.getId());
        projectDO.setDescription(validator.getDescription());
        return projectMapper.insert(projectDO) > 0;
    }

    @Override
    public boolean updateProject(ProjectDO project, CreateOrUpdateProjectDTO validator) {
        project.setName(validator.getName());
        project.setDescription(validator.getDescription());
        return projectMapper.updateById(project) > 0;
    }

    @Override
    public ProjectDO getById(Integer id) {
        return projectMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteProject(Integer id) {
        List<CaseDO> caseList = caseMapper.selectByProject(id);
        caseList.forEach(caseDO -> {
            caseMapper.deleteById(caseDO.getId());
        });
        return projectMapper.deleteById(id) > 0;
    }

    @Override
    public List<ProjectDO> getAll() {
        return projectMapper.getAll();
    }

    @Override
    public Integer getCaseCount(Integer id) {
        QueryWrapper<CaseDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project", id);
        return caseMapper.selectCount(queryWrapper);
    }
}
