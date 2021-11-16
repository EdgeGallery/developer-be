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
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.mapper.application.vm.VMMapper;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.application.vm.EnumVMStatus;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.google.gson.Gson;

@Service("vmAppVmService")
public class VMAppVmServiceImpl implements VMAppVmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppVmServiceImpl.class);

    private static Gson gson = new Gson();

    @Autowired
    VMMapper vmMapper;

    @Autowired
    VMAppOperationService vmAppOperationService;

    @Autowired
    ApplicationService applicationService;

    @Override
    public VirtualMachine createVm(String applicationId, VirtualMachine virtualMachine) {
        virtualMachine.setId(UUID.randomUUID().toString());
        virtualMachine.setStatus(EnumVMStatus.NOT_DEPLOY);
        int res = vmMapper.createVM(applicationId, virtualMachine);
        if (res < 1) {
            LOGGER.error("Create vm in db error.");
            throw new DataBaseException("Create vm in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        if (virtualMachine.getVmCertificate() != null) {
            vmMapper.createVMCertificate(virtualMachine.getId(), virtualMachine.getVmCertificate());
        }
        if (!CollectionUtils.isEmpty(virtualMachine.getPortList())) {
            for (VMPort port : virtualMachine.getPortList()) {
                createVmPort(virtualMachine.getId(), port);
            }
        }
        applicationService.updateApplicationStatus(applicationId, EnumApplicationStatus.CONFIGURED);
        return virtualMachine;
    }

    @Override
    public List<VirtualMachine> getAllVm(String applicationId) {
        List<VirtualMachine> virtualMachines = vmMapper.getAllVMsByAppId(applicationId);

        for (VirtualMachine virtualMachine : virtualMachines) {
            virtualMachine.setVmInstantiateInfo(vmAppOperationService.getInstantiateInfo(virtualMachine.getId()));
            virtualMachine.setImageExportInfo(vmAppOperationService.getImageExportInfo(virtualMachine.getId()));
            virtualMachine.setPortList(vmMapper.getAllVMPortsByVMId(virtualMachine.getId()));
            virtualMachine.setVmCertificate(vmMapper.getVMCertificate(virtualMachine.getId()));
        }
        return virtualMachines;
    }

    @Override
    public VirtualMachine getVm(String applicationId, String vmId) {
        VirtualMachine virtualMachine = vmMapper.getVMById(applicationId, vmId);
        if (virtualMachine == null) {
            LOGGER.error("vm is not exit.");
            throw new EntityNotFoundException("vm is not exit.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        virtualMachine.setVmCertificate(vmMapper.getVMCertificate(vmId));
        virtualMachine.setPortList(vmMapper.getAllVMPortsByVMId(vmId));
        virtualMachine.setImageExportInfo(vmAppOperationService.getImageExportInfo(virtualMachine.getId()));
        virtualMachine.setVmInstantiateInfo(vmAppOperationService.getInstantiateInfo(virtualMachine.getId()));
        return virtualMachine;
    }

    @Override
    public Boolean modifyVm(String applicationId, String vmId, VirtualMachine virtualMachine) {
        int res = vmMapper.modifyVM(virtualMachine);
        if (res < 1) {
            LOGGER.error("modify vm in db error.");
            throw new DataBaseException("modify vm in db error.", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        vmMapper.modifyVMCertificate(vmId, virtualMachine.getVmCertificate());
        //delete the port list and create the port list to modify ports.
        vmMapper.deleteAllVMPortsByVMId(vmId);
        for (VMPort vmPort : virtualMachine.getPortList()) {
            vmMapper.createVMPort(vmId, vmPort);
        }
        return true;
    }

    @Override
    public Boolean deleteVm(String applicationId, String vmId) {
        VirtualMachine getVm = getVm(applicationId, vmId);
        if (getVm == null || getVm.getVmInstantiateInfo() != null || getVm.getImageExportInfo() != null) {
            LOGGER.error("delete vm  fail, vm is not exit or is used,vmId:{}", vmId);
            throw new EntityNotFoundException("delete vm  fail, vm is not exit or is used.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        vmMapper.deleteVMCertificate(vmId);
        vmMapper.deleteVMPort(vmId);
        vmMapper.deleteVM(vmId);

        return true;
    }

    @Override
    public boolean updateVmStatus(String vmId, EnumVMStatus status, Integer targetImageId) {
        int res = vmMapper.updateVmStatus(vmId, status.toString(), targetImageId);
        if (res < 1) {
            LOGGER.error("update vm status fail");
            return false;
        }
        return true;
    }

    public int createVmPort(String vmId, VMPort port) {
        port.setId(UUID.randomUUID().toString());
        int res = vmMapper.createVMPort(vmId, port);
        if (res < 1) {
            LOGGER.error("Create VMPort in db error.");
            throw new DataBaseException("Create VMPort in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return res;
    }
}
