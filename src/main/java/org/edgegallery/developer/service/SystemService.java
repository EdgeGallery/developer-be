/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spencerwi.either.Either;

@Service("systemService")
public class SystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemService.class);

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    /**
     * createCapabilityGroup.
     *
     * @param capabilityGroup capabilityGroup
     * @return
     */
    @Transactional
    public Either<FormatRespDto, OpenMepCapabilityGroup> createCapabilityGroup(OpenMepCapabilityGroup capabilityGroup) {
        capabilityGroup.setGroupId(UUID.randomUUID().toString());
        if (StringUtils.isEmpty(capabilityGroup.getDescriptionEn())) {
            capabilityGroup.setDescriptionEn(capabilityGroup.getDescription());
        }
        if (StringUtils.isEmpty(capabilityGroup.getIconFileId())) {
            capabilityGroup.setIconFileId(capabilityGroup.getIconFileId());
        }
        if (StringUtils.isEmpty(capabilityGroup.getAuthor())) {
            capabilityGroup.setAuthor(capabilityGroup.getAuthor());
        }

        if (StringUtils.isEmpty(capabilityGroup.getOneLevelNameEn())) {
            capabilityGroup.setOneLevelNameEn(capabilityGroup.getOneLevelName());
        }
        if (StringUtils.isEmpty(capabilityGroup.getTwoLevelNameEn())) {
            capabilityGroup.setTwoLevelNameEn(capabilityGroup.getTwoLevelName());
        }
        capabilityGroup.setSelectCount(0);
        capabilityGroup.setUploadTime(new Date());
        int ret = openMepCapabilityMapper.saveGroup(capabilityGroup);
        if (ret <= 0) {
            LOGGER.error("save group {} failed!", capabilityGroup.getGroupId());
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save capability-group failed"));
        }

        for (OpenMepCapabilityDetail capability : capabilityGroup.getCapabilityDetailList()) {
            if (StringUtils.isBlank(capability.getApiFileId())) {
                LOGGER.error("Create {} detail failed, api file id is null", capabilityGroup.getGroupId());
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Api file id is wrong"));
            }
            if (StringUtils.isBlank(capability.getGuideFileId())) {
                LOGGER.error("Create {} detail failed, guide file id is null", capabilityGroup.getGroupId());
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "guide file id is wrong"));
            }
            capability.setGroupId(capabilityGroup.getGroupId());
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            capability.setUploadTime(time.format(new Date()));
            capability.setDetailId(UUID.randomUUID().toString());
            int result = openMepCapabilityMapper.saveCapability(capability);
            if (result > 0) {
                LOGGER.info("Create {} detail success", capabilityGroup.getGroupId());
                // update api file to un temp
                int api = uploadedFileMapper.updateFileStatus(capability.getApiFileId(), false);
                int guide = uploadedFileMapper.updateFileStatus(capability.getGuideFileId(), false);
                int guideEn = uploadedFileMapper.updateFileStatus(capability.getGuideFileIdEn(), false);
                if (api <= 0 || guide <= 0 || guideEn <= 0) {
                    String msg = "update api or guide or guide-en file status occur db error";
                    LOGGER.error(msg);
                    return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, msg));
                }
            } else {
                LOGGER.error("save capability {} failed!", capability.getDetailId());
                return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save capability-detail failed"));
            }
        }
        LOGGER.info("Create capability group {} success", capabilityGroup.getGroupId());
        return Either.right(capabilityGroup);

    }

    /**
     * modify OpenMepCapabilityGroup.
     *
     * @param groupId groupId
     * @param capabilityGroup input param
     * @return
     */
    @Transactional
    public Either<FormatRespDto, OpenMepCapabilityGroup> updateGroup(String groupId,
        OpenMepCapabilityGroup capabilityGroup) {
        if (StringUtils.isEmpty(groupId)) {
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "groupId is null"));
        }
        if (capabilityGroup == null) {
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "new capabilityGroup is null"));
        }
        OpenMepCapabilityGroup group = openMepCapabilityMapper.getOpenMepCapabilitiesByGroupId(groupId);
        if (group == null) {
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "groupId is incorrect"));
        }
        capabilityGroup.setGroupId(groupId);
        capabilityGroup.setUploadTime(new Date());
        int res = openMepCapabilityMapper.updateGroup(capabilityGroup);
        if (res < 1) {
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "update group failed!"));
        }

        List<OpenMepCapabilityDetail> newDetails = capabilityGroup.getCapabilityDetailList();
        List<OpenMepCapabilityDetail> details = group.getCapabilityDetailList();
        if (!CollectionUtils.isEmpty(details)) {
            OpenMepCapabilityDetail detail = details.get(0);
            //get old group's detailId
            String detailId = detail.getDetailId();
            if (!CollectionUtils.isEmpty(newDetails)) {
                OpenMepCapabilityDetail newDetail = newDetails.get(0);
                newDetail.setDetailId(detailId);
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                newDetail.setUploadTime(time.format(new Date()));
                newDetail.setGroupId(groupId);
                int resDetail = openMepCapabilityMapper.updateDetail(newDetail);
                if (resDetail < 1) {
                    return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "update detail failed!"));
                }
                int api = uploadedFileMapper.updateFileStatus(newDetail.getApiFileId(), false);
                int guide = uploadedFileMapper.updateFileStatus(newDetail.getGuideFileId(), false);
                int guideEn = uploadedFileMapper.updateFileStatus(newDetail.getGuideFileIdEn(), false);
                if (api <= 0 || guide <= 0 || guideEn <= 0) {
                    String msg = "update api or guide or guide-en file status occur db error";
                    return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, msg));
                }
            }
        }
        return Either.right(openMepCapabilityMapper.getOpenMepCapabilitiesByGroupId(groupId));
    }

    /**
     * deleteCapabilityByUserIdAndGroupId.
     */
    public Either<FormatRespDto, Boolean> deleteCapabilityByUserIdAndGroupId(String groupId) {
        List<OpenMepCapabilityDetail> capabilityDetailList = openMepCapabilityMapper.getDetailByGroupId(groupId);
        if (!CollectionUtils.isEmpty(capabilityDetailList)) {
            for (OpenMepCapabilityDetail capabilityDetail : capabilityDetailList) {
                int res = openMepCapabilityMapper.deleteCapability(capabilityDetail.getDetailId());
                if (res < 1) {
                    LOGGER.info("{} can not find", capabilityDetail.getDetailId());
                } else {
                    uploadedFileMapper.updateFileStatus(capabilityDetail.getApiFileId(), true);
                    uploadedFileMapper.updateFileStatus(capabilityDetail.getGuideFileId(), true);
                    uploadedFileMapper.updateFileStatus(capabilityDetail.getGuideFileIdEn(), true);
                    LOGGER.info("Delete capability detail {} success", capabilityDetail.getDetailId());
                }
            }
        }
        int res = openMepCapabilityMapper.deleteGroup(groupId);
        if (res < 1) {
            LOGGER.info("{} can not find", groupId);
        } else {
            LOGGER.info("Delete group {} success", groupId);
        }
        return Either.right(true);
    }

    /**
     * getAllCapabilityGroups.
     */
    public Page<OpenMepCapabilityGroup> getAllCapabilityGroups(String userId, String twoLevelName,
        String twoLevelNameEn, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<OpenMepCapabilityGroup> pageInfo = new PageInfo<>(
            openMepCapabilityMapper.getOpenMepListByCondition(userId, twoLevelName, twoLevelNameEn));
        LOGGER.info("Get all capability groups success.");
        return new Page<OpenMepCapabilityGroup>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * getCapabilityByGroupId.
     */
    public Either<FormatRespDto, OpenMepCapabilityGroup> getCapabilityByGroupId(String groupId) {
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
}
