package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;

import java.util.List;


public interface ProjectService {

    List<ProjectDO> getProjectByName(String name);
}
