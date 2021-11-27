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

package org.edgegallery.developer.service.capability.impl;

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.mapper.uploadfile.UploadFileMapper;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.service.capability.CapabilityGroupService;
import org.edgegallery.developer.service.capability.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("v2-capabilityService")
public class CapabilityServiceImpl implements CapabilityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityServiceImpl.class);

    @Autowired
    private CapabilityMapper capabilityMapper;

    @Autowired
    private CapabilityGroupService capbilityGroupService;

    @Autowired
    private UploadFileMapper uploadFileMapper;


    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Capability create(Capability capability) {
        String name = capability.getName();
        if (StringUtils.isBlank(name)) {
            LOGGER.error("Create capability {} , name is null", capability.getName());
            throw new IllegalRequestException("capability name is null", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (StringUtils.isEmpty(capability.getDescriptionEn())) {
            capability.setDescriptionEn(capability.getDescription());
        }
        if (StringUtils.isEmpty(capability.getNameEn())) {
            capability.setNameEn(capability.getName());
        }
        if (StringUtils.isBlank(capability.getApiFileId())) {
            LOGGER.error("Create capability {} , api file id is null", name);
            throw new IllegalRequestException("Api file id is wrong", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (StringUtils.isBlank(capability.getGuideFileId())) {
            LOGGER.error("Create capability {} failed, guide file id is null", name);
            throw new IllegalRequestException("Guide file id is wrong", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        if (StringUtils.isBlank(capability.getIconFileId())) {
            LOGGER.error("Create capability {} failed, icon file id is null", name);
            throw new IllegalRequestException("Icon file id is wrong", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }

        // CapabilityGroup check begin
        CapabilityGroup group = capability.getGroup();
        if (group == null) {
            LOGGER.info("The capability {} group is null.", name);
            return capability;
        }

        String groupId = group.getId();
        if (StringUtils.isEmpty(groupId)) {
            // Create CapibilityGroup
            List<CapabilityGroup> findGroups = capbilityGroupService
                .findByNameOrNameEn(group.getName(), group.getNameEn());
            if (CollectionUtils.isEmpty(findGroups)) {
                // not exist,then create it.
                CapabilityGroup result = capbilityGroupService.create(group);
                if (result == null) {
                    LOGGER.error("create group failed!");
                    throw new DataBaseException("create group failed!", ResponseConsts.RET_CERATE_DATA_FAIL);
                }
            } else {
                if (findGroups.size() == 1) {
                    CapabilityGroup findGroup = findGroups.get(0);
                    capability.setGroupId(findGroup.getId());
                } else {
                    String errMsg = "Find capability group size {} ,{}/{} is conflict.";
                    LOGGER.error(errMsg, findGroups.size(), group.getName(), group.getNameEn());
                    throw new IllegalRequestException("Find capability group size can not greater than 2",
                        ResponseConsts.RET_QUERY_DATA_FAIL);

                }
            }
        } else {
            // Validate the groupId
            CapabilityGroup capabilityGroup = capbilityGroupService.findById(groupId);
            if (capabilityGroup == null) {
                LOGGER.error("Create capability {} failed,capability group id {} is invalid.", name, groupId);
                throw new IllegalRequestException("capability groupId is invalid", ResponseConsts.RET_QUERY_DATA_EMPTY);
            }
        }
        // CapbilityGroup check end

        List<Capability> findedCapabilities = capabilityMapper.selectByNameOrNameEn(name, capability.getNameEn());
        if (!CollectionUtils.isEmpty(findedCapabilities)) {
            LOGGER.error("The capability name {} has exist.", capability.getName());
            throw new IllegalRequestException("The capability already exists", ResponseConsts.RET_QUERY_DATA_FAIL);
        }

        capability.setUploadTime(System.currentTimeMillis());
        capability.setId(UUID.randomUUID().toString());
        int result = capabilityMapper.insert(capability);
        if (result > 0) {
            LOGGER.info("Create capability {} success", capability.getName());
            // update api file to un temp
            int api = uploadFileMapper.updateFileStatus(capability.getApiFileId(), false);
            int guide = uploadFileMapper.updateFileStatus(capability.getGuideFileId(), false);
            int guideEn = uploadFileMapper.updateFileStatus(capability.getGuideFileIdEn(), false);
            int icon = uploadFileMapper.updateFileStatus(capability.getIconFileId(), false);
            if (api <= 0 || guide <= 0 || guideEn <= 0 || icon <= 0) {
                String msg = "update api or guide or guide-en or icon file status occur db error";
                LOGGER.error(msg);
                throw new DeveloperException(msg);
            }
        } else {
            LOGGER.error("save capability {} failed!", capability.getName());
            throw new DeveloperException("Create capability failed");
        }
        return capability;
    }

    @Transactional
    @Override
    public Capability updateById(Capability capability) {
        capability.setUploadTime(System.currentTimeMillis());
        int ret = capabilityMapper.updateById(capability);
        if (ret <= 0) {
            LOGGER.error("Update capability {} failed!{}", capability.getId(), capability.getName());
            throw new DataBaseException("Update capability failed.", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        int api = uploadFileMapper.updateFileStatus(capability.getApiFileId(), false);
        int guide = uploadFileMapper.updateFileStatus(capability.getGuideFileId(), false);
        int guideEn = uploadFileMapper.updateFileStatus(capability.getGuideFileIdEn(), false);
        int icon = uploadFileMapper.updateFileStatus(capability.getIconFileId(), false);
        if (api <= 0 || guide <= 0 || guideEn <= 0 || icon <= 0) {
            String msg = "update api or guide or guide-en file status occur db error";
            LOGGER.error(msg);
            throw new DataBaseException(msg, ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return capability;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public boolean deleteById(String id) {
        Capability capability = capabilityMapper.selectById(id);
        if (capability == null) {
            LOGGER.error("Delete capability {} is not exist!", id);
            return true;
        }

        int ret = capabilityMapper.deleteById(id);
        if (ret <= 0) {
            LOGGER.error("Delete capability {} failed!", id);
            throw new DataBaseException("Delete capability failed.", ResponseConsts.RET_DELETE_DATA_FAIL);
        }

        uploadFileMapper.updateFileStatus(capability.getApiFileId(), true);
        uploadFileMapper.updateFileStatus(capability.getGuideFileId(), true);
        uploadFileMapper.updateFileStatus(capability.getGuideFileIdEn(), true);

        String groupId = capability.getGroupId();
        if (StringUtils.isEmpty(groupId)) {
            LOGGER.info("Delete capability {} success", capability.getName());
            return true;
        }

        List<Capability> remainCapabilities = this.findByGroupId(groupId);
        if (remainCapabilities.isEmpty()) {
            boolean deleteCapabilityGroupResult = capbilityGroupService.deleteById(groupId);
            if (!deleteCapabilityGroupResult) {
                LOGGER.error("delete group {} failed!", groupId);
                throw new DataBaseException("delete group failed", ResponseConsts.RET_DELETE_DATA_FAIL);
            }
        }

        LOGGER.info("Delete capability {} success", capability.getName());
        return true;
    }

    @Override
    public List<Capability> findAll() {
        return capabilityMapper.selectAll();
    }

    @Override
    public List<Capability> findByType(String type) {
        return capabilityMapper.selectByType(type);
    }

    @Override
    public Capability findById(String id) {
        return capabilityMapper.selectById(id);
    }

    @Override
    public List<Capability> findByGroupId(String groupId) {
        return capabilityMapper.selectByGroupId(groupId);
    }

    @Override
    public List<Capability> findByProjectId(String projectId) {
        return capabilityMapper.selectByProjectId(projectId);
    }

    @Override
    public Capability findByName(String name) {
        return capabilityMapper.selectByName(name);
    }

    @Override
    public List<Capability> findByApiFileId(String apiFileId) {
        return capabilityMapper.selectByApiFileId(apiFileId);
    }

    @Override
    public List<Capability> findByNameWithFuzzy(String name) {
        return capabilityMapper.selectByNameWithFuzzy(name);
    }

    @Override
    public List<Capability> findByNameEnWithFuzzy(String nameEn) {
        return capabilityMapper.selectByNameEnWithFuzzy(nameEn);
    }

    @Override
    public List<Capability> findByNameOrNameEn(String name, String nameEn) {
        return capabilityMapper.selectByNameOrNameEn(name, nameEn);
    }

    @Override
    public boolean updateSelectCountByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }

        return capabilityMapper.updateSelectCountByIds(ids) > 0;
    }
}
