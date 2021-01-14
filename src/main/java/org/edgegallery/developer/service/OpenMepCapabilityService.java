/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.workspace.OpenMepApi;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroups;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.OpenMepApiResponse;
import org.edgegallery.developer.response.OpenMepEcoApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("openMepCapabilityService")
public class OpenMepCapabilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenMepCapabilityService.class);

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    /**
     * getAll.
     *
     * @return
     */
    public OpenMepCapabilityGroups getAll(String type) {
        List<OpenMepCapabilityGroup> list = null;
        if ("detail".equalsIgnoreCase(type)) {
            list = openMepCapabilityMapper.getOpenMepCapabilitiesDetail();
        } else {
            list = openMepCapabilityMapper.getOpenMepCapabilities();
        }
        OpenMepCapabilityGroups all = new OpenMepCapabilityGroups();
        all.setValues(list);
        return all;
    }

    /**
     * getAllCapabilityGroups.
     *
     * @return
     */
    public Either<FormatRespDto, List<OpenMepCapabilityGroup>> getAllCapabilityGroups() {
        List<OpenMepCapabilityGroup> list = openMepCapabilityMapper.getOpenMepCapabilities();
        LOGGER.info("Get all capability groups success");
        return Either.right(list);
    }

    /**
     * getCapabilitiesByGroupId.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepCapabilityGroup> getCapabilitiesByGroupId(String groupId) {
        OpenMepCapabilityGroup group = openMepCapabilityMapper.getOpenMepCapabilitiesByGroupId(groupId);
        if (group != null) {
            List<OpenMepCapabilityDetail> details = group.getCapabilityDetailList();
            if (details != null) {
                Iterator<OpenMepCapabilityDetail> iterator = details.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getDetailId() == null) {
                        iterator.remove();
                    }
                }
            }
            LOGGER.info("Get capability by {} success", groupId);
            return Either.right(group);
        }
        LOGGER.error("Can not get capability by {}", groupId);
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get capabilities by group failed"));
    }

    /**
     * createGroup.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepCapabilityGroup> createGroup(OpenMepCapabilityGroup group) {
        group.setGroupId(UUID.randomUUID().toString());
        int ret = openMepCapabilityMapper.saveGroup(group);
        if (ret > 0) {
            LOGGER.info("Create group {} success", group.getGroupId());
            return Either.right(openMepCapabilityMapper.getGroup(group.getGroupId()));
        }
        LOGGER.error("Create group failed");
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Create group failed."));
    }

    /**
     * createCapability.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepCapabilityDetail> createCapability(String groupId,
        OpenMepCapabilityDetail capability) {
        OpenMepCapabilityGroup group = openMepCapabilityMapper.getGroup(groupId);
        if (group == null) {
            LOGGER.error("Can not find capability by {}", groupId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not find Capability group."));
        }
        if (capability.getApiFileId() == null || capability.getApiFileId().length() < 1) {
            LOGGER.error("Create {} detail failed, api file id is null", groupId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Api file id is wrong"));
        }
        if (capability.getGuideFileId() == null || capability.getGuideFileId().length() < 1) {
            LOGGER.error("Create {} detail failed, guide file id is null", groupId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "guide file id is wrong"));
        }
        capability.setGroupId(groupId);
        capability.setUploadTime(new Date());
        capability.setDetailId(UUID.randomUUID().toString());
        int ret = openMepCapabilityMapper.saveCapability(capability);
        if (ret > 0) {
            LOGGER.info("Create {} detail success", groupId);
            // update api file to un temp
            uploadedFileMapper.updateFileStatus(capability.getApiFileId(), false);
            uploadedFileMapper.updateFileStatus(capability.getGuideFileId(), false);
            return Either.right(openMepCapabilityMapper.getDetail(capability.getDetailId()));
        }
        LOGGER.error("Create {} detail failed", groupId);
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not create a host."));
    }

    /**
     * deleteGroup.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteGroup(String groupId) {
        int res = openMepCapabilityMapper.deleteGroup(groupId);
        if (res < 1) {
            LOGGER.info("{} can not find", groupId);
        } else {
            LOGGER.info("Delete group {} success", groupId);
        }
        return Either.right(true);
    }

    /**
     * deleteCapability by userId.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteCapabilityByUserId(String capabilityId, String userId) {
        OpenMepCapabilityDetail openMepCapabilityDetail = openMepCapabilityMapper.getDetail(capabilityId);
        if (!openMepCapabilityDetail.getUserId().equals(userId)) {
            LOGGER.info("The user is not the owner of the capability");
            return Either.right(true);
        }
        int res = openMepCapabilityMapper.deleteCapability(capabilityId);
        if (res < 1) {
            LOGGER.info("{} can not find", capabilityId);
        } else {
            LOGGER.info("Delete capability {} success", capabilityId);
        }
        return Either.right(true);
    }

    /**
     * getOpenMepList.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepApiResponse> getOpenMepList(String type) {
        List<OpenMepCapabilityGroup> list = openMepCapabilityMapper.getOpenMepList(type);
        OpenMepApiResponse res = new OpenMepApiResponse();
        res.setOpenCapability(list);
        return Either.right(res);
    }

    /**
     * getOpenMepEcoList.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepEcoApiResponse> getOpenMepEcoList() {
        List<OpenMepApi> list = openMepCapabilityMapper.getOpenMepEcoList();
        OpenMepEcoApiResponse res = new OpenMepEcoApiResponse();
        res.setOpenMepEcos(list);
        return Either.right(res);
    }

    /**
     * getOpenMep by fileId.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepCapabilityDetail> getOpenMepByFileId(String fileId) {
        OpenMepCapabilityDetail res = openMepCapabilityMapper.getOpenMepByFileId(fileId);
        return Either.right(res);
    }

}
