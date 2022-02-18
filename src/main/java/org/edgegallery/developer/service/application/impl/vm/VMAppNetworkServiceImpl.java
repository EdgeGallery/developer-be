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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.vm.NetworkMapper;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vmAppNetworkService")
public class VMAppNetworkServiceImpl implements VMAppNetworkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppNetworkServiceImpl.class);

    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private VMAppVmService vmAppVmService;

    @Override
    public Network createNetwork(String applicationId, Network network) {
        network.setId(UUID.randomUUID().toString());
        int res = networkMapper.createNetwork(applicationId, network);
        if (res < 1) {
            LOGGER.error("Create network in db error.");
            throw new DataBaseException("Create network in db error.", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return network;
    }

    @Override
    public List<Network> getAllNetwork(String applicationId) {
        List<Network> networks = networkMapper.getNetworkByAppId(applicationId);
        networks.sort(new Comparator<Network>() {
            @Override
            public int compare(Network o1, Network o2) {
                int index1 = AppdConstants.NETWORK_NAME_SORTED_LIST.indexOf(o1.getName());
                int index2 = AppdConstants.NETWORK_NAME_SORTED_LIST.indexOf(o2.getName());
                return index1 - index2;
            }
        });
        return networks;
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
            throw new DataBaseException("modify network in db error.", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean deleteNetwork(String applicationId, String networkId) {
        //check network used by port
        Network network = networkMapper.getNetworkById(networkId);
        List<VirtualMachine> vms = vmAppVmService.getAllVm(applicationId);
        if (isNetworkUsedByVMPorts(network, vms)) {
            throw new DeveloperException("Network is used by vm port. Cannot be deleted",
                ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        networkMapper.deleteNetwork(networkId);
        return true;
    }

    @Override
    public boolean deleteNetworkByAppId(String applicationId) {
        //check network used by port
        List<Network> networks = networkMapper.getNetworkByAppId(applicationId);
        if (networks.isEmpty()) {
            return false;
        }
        List<VirtualMachine> vms = vmAppVmService.getAllVm(applicationId);
        for (Network network : networks) {
            if (isNetworkUsedByVMPorts(network, vms)) {
                throw new DeveloperException("Network is used by vm port. Cannot be deleted",
                    ResponseConsts.RET_DELETE_DATA_FAIL);
            }
        }
        int res = networkMapper.deleteNetworksByAppId(applicationId);
        if (res < 1) {
            LOGGER.error("delete network in db error.");
            throw new DataBaseException("delete network in db error.", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        return true;
    }

    private boolean isNetworkUsedByVMPorts(Network network, List<VirtualMachine> vms) {
        for (VirtualMachine vm : vms) {
            for (VMPort port : vm.getPortList()) {
                if (network.getName().equals(port.getNetworkName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
