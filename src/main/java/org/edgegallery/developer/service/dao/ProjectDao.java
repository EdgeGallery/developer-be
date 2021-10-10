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

package org.edgegallery.developer.service.dao;

import com.spencerwi.either.Either;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.mapper.ProjectCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProjectDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDao.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private ProjectCapabilityMapper projectCapabilityMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    /**
     * delete project from db.
     *
     * @param userId user id
     * @param projectId project id
     * @return true or error
     */
    @Transactional
    public Either<FormatRespDto, Boolean> deleteProject(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.info("Can not find project by userId {} and projectId {}, do not need delete.", userId, projectId);
            return Either.right(true);
        }

        // delete project and test config
        int res = projectMapper.delete(projectId);
        if (res < 1) {
            LOGGER.error("Delete project {} failed.", projectId);
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete project failed."));
        }

        // delete icon file
        int iconRes = uploadedFileMapper.deleteFile(project.getIconFileId());
        if (iconRes < 1) {
            LOGGER.warn("Failed to delete icon file.");
        }

        String testId = project.getLastTestId();
        if (testId == null) {
            return Either.right(true);
        }
        ProjectTestConfig testConfig = projectMapper.getTestConfig(testId);
        if (testConfig == null) {
            LOGGER.warn("Can not find test config of project {}", projectId);
            return Either.right(true);
        }

        // delete vm config
        int vmRes = vmConfigMapper.deleteVmCreateConfigs(projectId);
        if (vmRes >= 1) {
            LOGGER.info("delete vm config success {}", projectId);
        }

        // delete api file
        int apiRes = uploadedFileMapper.deleteFile(testConfig.getAppApiFileId());
        if (apiRes < 1) {
            LOGGER.warn("Failed to delete api file.");
        }

        // delete open mep capability
        String openCapabilityId = project.getOpenCapabilityId();
        if (openCapabilityId == null) {
            return Either.right(true);
        }

        int capabilityRes = projectCapabilityMapper.deleteByProjectId(projectId);
        if (capabilityRes < 1) {
            LOGGER.warn("Delete open mep capability {} failed.", openCapabilityId);
        }
        return Either.right(true);
    }

}
