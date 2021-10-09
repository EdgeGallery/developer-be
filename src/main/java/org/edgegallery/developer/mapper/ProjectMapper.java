/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;

@Mapper
public interface ProjectMapper {

    int save(ApplicationProject project);

    int delete(String projectId);

    ApplicationProject getProject(String userId, String projectId);

    ApplicationProject getProjectById(String projectId);
    
    List<ApplicationProject> getAllProject();

    List<ApplicationProject> getProjectByNameWithFuzzy(@Param("userId")String userId,@Param("name") String name);

    List<ApplicationProject> getAllProjectNoCondtion();

    int countProjects(String userId);

    int updateProject(ApplicationProject project);

    int saveTestConfig(ProjectTestConfig testConfig);

    ProjectTestConfig getTestConfig(String testId);

    int updateTestConfig(ProjectTestConfig testConfig);

    int modifyTestConfig(ProjectTestConfig testConfig);

    List<ProjectTestConfig> getTestConfigByProjectId(String projectId);

    List<ProjectTestConfig> getTestConfigByDeployStatus(String deployStatus);

    void updateProjectStatus(@Param("id") String id, @Param("status") String status);
}
