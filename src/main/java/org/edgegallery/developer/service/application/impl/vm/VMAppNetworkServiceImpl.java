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
package org.edgegallery.developer.service.application.impl.vm;

import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.vm.NetworkMapper;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service("vmAppNetworkService")
public class VMAppNetworkServiceImpl implements VMAppNetworkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    NetworkMapper networkMapper;

    @Override
    public Network createNetwork(String applicationId, Network network) {
        network.setId(UUID.randomUUID().toString());
        int res = networkMapper.createNetwork(applicationId, network);
        if (res < 1) {
            LOGGER.error("Create network in db error.");
            throw new DeveloperException("Create network in db error.", ResponseConsts.INSERT_DATA_FAILED);
        }
        return network;
    }

    @Override
    public List<Network> getAllNetwork(String applicationId) {
        return networkMapper.getNetworkByAppId(applicationId);
    }

    @Override
    public Network getNetwork(String applicationId, String networkId) {
        return networkMapper.getNetworkById(networkId);

    }

    @Override
    public Boolean modifyNetwork(String applicationId, String networkId, Network network) {
        int res = networkMapper.modifyNetwork(network);
        if (res < 1) {
            LOGGER.error("modify network in db error.");
            throw new DeveloperException("modify network in db error.", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public Boolean deleteNetwork(String applicationId, String networkId) {
        int res = networkMapper.deleteNetwork(networkId);
        if (res < 1) {
            LOGGER.error("delete network in db error.");
            throw new DeveloperException("delete network in db error.", ResponseConsts.DELETE_DATA_FAILED);
        }
        return true;
    }
}
