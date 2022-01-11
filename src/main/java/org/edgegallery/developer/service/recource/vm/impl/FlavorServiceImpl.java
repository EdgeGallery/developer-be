/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.service.recource.vm.impl;

import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.mapper.resource.vm.FlavorMapper;
import org.edgegallery.developer.model.resource.vm.Flavor;
import org.edgegallery.developer.service.recource.vm.FlavorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("flavorService")
public class FlavorServiceImpl implements FlavorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlavorServiceImpl.class);

    @Autowired
    FlavorMapper flavorMapper;


    @Override
    public List<Flavor> getAllFavors() {
        return flavorMapper.getAllFavors();
    }

    @Override
    public Flavor getFavorById(String flavorId) {
        return flavorMapper.getFavorById(flavorId);
    }

    @Override
    public Flavor createFavor(Flavor flavor) {
        flavor.setId(UUID.randomUUID().toString());
        int res = flavorMapper.createFavor(flavor);
        if (res < 1) {
            LOGGER.error("Create flavor in db error.");
            throw new DataBaseException("Create flavor in db error.", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return flavor;
    }

    @Override
    public Boolean deleteFavorById(String flavorId) {
        int res = flavorMapper.deleteFavorById(flavorId);
        if (res < 1) {
            LOGGER.error("delete flavor in db error.");
            throw new DataBaseException("delete flavor in db error.", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return true;
    }
}
