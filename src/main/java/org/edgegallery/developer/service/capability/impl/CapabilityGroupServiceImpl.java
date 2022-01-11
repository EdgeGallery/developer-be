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
import org.edgegallery.developer.mapper.capability.CapabilityGroupMapper;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.service.capability.CapabilityGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("v2-capabilityGroupService")
public class CapabilityGroupServiceImpl implements CapabilityGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityGroupServiceImpl.class);

    @Autowired
    private CapabilityGroupMapper capabilityGroupMapper;

    @Override
    public List<CapabilityGroup> findByNameOrNameEn(String name, String nameEn) {
        return capabilityGroupMapper.selectByNameOrNameEn(name, nameEn);
    }

    @Override
    public CapabilityGroup create(CapabilityGroup capabilityGroup) {
        capabilityGroup.setId(UUID.randomUUID().toString());

        if (StringUtils.isEmpty(capabilityGroup.getNameEn())) {
            capabilityGroup.setName(capabilityGroup.getName());
        }

        if (StringUtils.isEmpty(capabilityGroup.getDescriptionEn())) {
            capabilityGroup.setDescriptionEn(capabilityGroup.getDescription());
        }

        long currTime = System.currentTimeMillis();
        capabilityGroup.setCreateTime(currTime);
        capabilityGroup.setUpdateTime(currTime);
        int ret = capabilityGroupMapper.insert(capabilityGroup);
        if (ret <= 0) {
            LOGGER.error("Create capabilityGroup {} failed!", capabilityGroup.getName());
            throw new DataBaseException("Create capability group failed.", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return capabilityGroup;
    }

    @Override
    public CapabilityGroup updateById(CapabilityGroup capabilityGroup) {
        int ret = capabilityGroupMapper.updateById(capabilityGroup);
        if (ret <= 0) {
            LOGGER.error("Update capabilityGroup {} failed!", capabilityGroup.getName());
            throw new DataBaseException("Update capability group failed.", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return capabilityGroup;
    }

    @Override
    public boolean deleteById(String groupId) {
        int ret = capabilityGroupMapper.deleteById(groupId);
        if (ret <= 0) {
            LOGGER.error("Delete capabilityGroup {} failed!", groupId);
            throw new DataBaseException("Delete capability group failed.", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public List<CapabilityGroup> findByType(String type) {
        return capabilityGroupMapper.selectByType(type);
    }

    @Override
    public List<CapabilityGroup> findAll() {
        return capabilityGroupMapper.selectAll();
    }

    @Override
    public CapabilityGroup findById(String id) {
        return capabilityGroupMapper.selectById(id);
    }

    @Override
    public CapabilityGroup findByName(String name) {
        return capabilityGroupMapper.selectByName(name);
    }
}
