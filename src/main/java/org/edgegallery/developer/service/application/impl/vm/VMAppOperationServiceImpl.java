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

package org.edgegallery.developer.service.application.impl.vm;

import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.mapper.application.vm.VMInstantiateInfoMapper;
import org.edgegallery.developer.mapper.application.vm.VMMapper;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.vm.VMExportImageOperation;
import org.edgegallery.developer.service.application.action.impl.vm.VMLaunchOperation;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.apppackage.csar.VMPackageFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("vmAppOperationService")
public class VMAppOperationServiceImpl extends AppOperationServiceImpl implements VMAppOperationService {

    public static final String OPERATION_LAUNCH_NAME = "VirtualMachine launch";

    public static final String OPERATION_EXPORT_IMAGE_NAME = "VirtualMachine Export Image";

    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppOperationServiceImpl.class);

    @Autowired
    VMInstantiateInfoMapper vmInstantiateInfoMapper;

    @Autowired
    ImageExportInfoMapper imageExportInfoMapper;

    @Autowired
    OperationStatusMapper operationStatusMapper;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    VMAppVmServiceImpl vmAppVmServiceImpl;

    @Autowired
    AppPackageService appPackageService;

    @Autowired
    VMMapper vmMapper;

    @Override
    public OperationInfoRep instantiateVM(String applicationId, String vmId, String accessToken) {

        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application is not exited,id:{}", applicationId);
            throw new EntityNotFoundException("application is not exited.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        VirtualMachine virtualMachine = vmAppVmServiceImpl.getVm(applicationId, vmId);
        if (virtualMachine == null || virtualMachine.getVmInstantiateInfo() != null
            || virtualMachine.getImageExportInfo() != null) {
            LOGGER.error("instantiate vm app fail ,vm is not exit or is used,vmId:{}", vmId);
            throw new EntityNotFoundException("instantiate vm app fail ,vm is not exit or is used.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setUserName(AccessUserUtil.getUser().getUserName());
        operationStatus.setObjectType(EnumOperationObjectType.APPLICATION_INSTANCE);
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setObjectId(vmId);
        operationStatus.setObjectName(virtualMachine.getName());
        operationStatus.setOperationName(OPERATION_LAUNCH_NAME);
        int res = operationStatusMapper.createOperationStatus(operationStatus);
        if (res < 1) {
            LOGGER.error("Create instantiate vm operationStatus in db error.");
            throw new DataBaseException("Create instantiate vm operationStatus in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        VMLaunchOperation actionCollection = new VMLaunchOperation(AccessUserUtil.getUser(), applicationId, vmId,
            accessToken, operationStatus);
        LOGGER.info("start instantiate vm app");
        new InstantiateVmAppProcessor(actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }

    @Override
    public Boolean uploadFileToVm(String applicationId, String vmId, HttpServletRequest request, Chunk chunk) {
        return null;
    }

    @Override
    public ResponseEntity mergeAppFile(String applicationId, String vmId, String fileName, String identifier) {
        return null;
    }

    @Override
    public OperationInfoRep createImageExport(String applicationId, String vmId, String accessToken) {
        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application is not exited,id:{}", applicationId);
            throw new EntityNotFoundException("application is not exited.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        VirtualMachine virtualMachine = vmAppVmServiceImpl.getVm(applicationId, vmId);
        if (virtualMachine == null || virtualMachine.getVmInstantiateInfo() == null
            || virtualMachine.getImageExportInfo() != null) {
            LOGGER.error("instantiate vm app fail ,vm is not exit or is used,vmId:{}", vmId);
            throw new EntityNotFoundException("instantiate vm app fail ,vm is not exit or is used.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setUserName(AccessUserUtil.getUser().getUserName());
        operationStatus.setObjectType(EnumOperationObjectType.VM_IMAGE_INSTANCE);
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setObjectId(vmId);
        operationStatus.setObjectName(virtualMachine.getName());
        operationStatus.setOperationName(OPERATION_EXPORT_IMAGE_NAME);
        int res = operationStatusMapper.createOperationStatus(operationStatus);
        if (res < 1) {
            LOGGER.error("Create export image operationStatus in db error.");
            throw new DataBaseException("Create export image operationStatus in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        VMExportImageOperation actionCollection = new VMExportImageOperation(AccessUserUtil.getUser(), applicationId, vmId,
            accessToken, operationStatus);
        LOGGER.info("start instantiate vm app");
        new ExportVmImageProcessor(actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }

    @Override
    public AppPackage generatePackage(String applicationId) {
        ApplicationDetail detail = applicationService.getApplicationDetail(applicationId);
        return generatePackage(detail.getVmApp());
    }

    @Override
    public AppPackage generatePackage(VMApplication application) {
        return appPackageService.generateAppPackage(application);
    }

    public VMInstantiateInfo getInstantiateInfo(String vmId) {
        VMInstantiateInfo instantiateInfo = vmInstantiateInfoMapper.getVMInstantiateInfo(vmId);
        if (instantiateInfo != null) {
            List<PortInstantiateInfo> portLst = vmInstantiateInfoMapper.getPortInstantiateInfoByVMId(vmId);
            instantiateInfo.setPortInstanceList(portLst);
        }
        return instantiateInfo;
    }

    public Boolean updateInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo) {
        int res = vmInstantiateInfoMapper.modifyVMInstantiateInfo(vmId, instantiateInfo);
        if (res < 1) {
            LOGGER.error("Update vm instantiate info failed");
            return false;
        }
        //update ports
        res = vmInstantiateInfoMapper.deleteVMInstantiateInfo(vmId);
        if (res < 1) {
            LOGGER.error("Update vm instantiate info failed, remove ports failed.");
            return false;
        }
        for (PortInstantiateInfo port : instantiateInfo.getPortInstanceList()) {
            res = vmInstantiateInfoMapper.createPortInstantiateInfo(vmId, port);
            if (res < 1) {
                LOGGER.error("Update vm instantiate info failed, add port instances failed.");
                return false;
            }
        }
        return true;
    }

    public ImageExportInfo getImageExportInfo(String vmId) {
        return imageExportInfoMapper.getImageExportInfoInfoByVMId(vmId);
    }

    public static class InstantiateVmAppProcessor extends Thread {

        VMLaunchOperation actionCollection;

        public InstantiateVmAppProcessor(VMLaunchOperation actionCollection) {
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            IActionIterator iterator = actionCollection.getActionIterator();
            while (iterator.hasNext()) {
                IAction action = iterator.nextAction();
                boolean result = action.execute();
                if (!result) {
                    break;
                }
            }
        }

    }
    public static class ExportVmImageProcessor extends Thread {

        VMExportImageOperation actionCollection;

        public ExportVmImageProcessor(VMExportImageOperation actionCollection) {
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            IActionIterator iterator = actionCollection.getActionIterator();
            while (iterator.hasNext()) {
                IAction action = iterator.nextAction();
                boolean result = action.execute();
                if (!result) {
                    break;
                }
            }
        }
    }

}
