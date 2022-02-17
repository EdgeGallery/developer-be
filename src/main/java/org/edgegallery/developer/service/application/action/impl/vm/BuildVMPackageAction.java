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

package org.edgegallery.developer.service.application.action.impl.vm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildVMPackageAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildVMPackageAction.class);

    public static final String ACTION_NAME = "Build Application Package";

    private VMAppOperationServiceImpl vmAppOperationService = (VMAppOperationServiceImpl) SpringContextUtil
        .getBean(VMAppOperationServiceImpl.class);

    private ApplicationService applicationService = (ApplicationService) SpringContextUtil
        .getBean(ApplicationService.class);

    private static final String SET_PWD_FILE_PATH = "./configs/template/user_data/setpwd.txt";

    private static final String BASH_TITLE = "#!/bin/bash";

    private static final String USER_NAME_PARAM_STR = "\\$USERNAME\\$";

    private static final String PASSWORD_PARAM_STR = "\\$PASSWORD\\$";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        //Start action , save action status.
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        String statusLog = "Start to build the package for vm: " + vmId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.VM, vmId, ACTION_NAME, statusLog);

        //create new application object with single vm.
        ApplicationDetail detail = applicationService.getApplicationDetail(applicationId);
        VMApplication tempApp = detail.getVmApp();
        tempApp.setId(UUID.randomUUID().toString());
        for (VirtualMachine vm : tempApp.getVmList()) {
            if (vmId.equals(vm.getId())) {
                tempApp.getVmList().clear();
                updateUserContentsToModifyPwd(vm);
                tempApp.getVmList().add(vm);
                break;
            }
        }
        statusLog = "Build application for single vm finished.";
        LOGGER.info(statusLog);
        updateActionProgress(actionStatus, 25, statusLog);

        //build application package for launch VM.
        AppPackage appPkg = vmAppOperationService.generatePackage(tempApp);
        if (appPkg == null) {
            statusLog = "Build package for VM failed.";
            LOGGER.error(statusLog);
            updateActionError(actionStatus, statusLog);
            return false;
        }
        getContext().addParameter(IContextParameter.PARAM_PACKAGE_ID, appPkg.getId());
        statusLog = "Build package for single vm finished.";
        LOGGER.info(statusLog);
        updateActionProgress(actionStatus, 50, statusLog);

        boolean res = saveBuildVmPackageInfo(vmId, appPkg.getId());
        if (!res) {
            updateActionError(actionStatus, "Update instantiate info for VM failed.");
            return false;
        }
        statusLog = "Update package info to vm instantiate info finished.";
        updateActionProgress(actionStatus, 100, statusLog);
        LOGGER.info("Build VM package action finished.");
        return true;
    }

    private void updateUserContentsToModifyPwd(VirtualMachine vm) {
        if (null == vm.getVmCertificate() || null == vm.getVmCertificate().getPwdCertificate() || StringUtils
            .isEmpty(vm.getVmCertificate().getPwdCertificate().getUsername())) {
            return;
        }
        String setPwdScript = "";
        try {
            setPwdScript = FileUtils.readFileToString(new File(SET_PWD_FILE_PATH), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Get set pwd script failed.", e);
            return;
        }
        setPwdScript = setPwdScript
            .replaceAll(USER_NAME_PARAM_STR, vm.getVmCertificate().getPwdCertificate().getUsername());
        setPwdScript = setPwdScript
            .replaceAll(PASSWORD_PARAM_STR, vm.getVmCertificate().getPwdCertificate().getPassword());
        String userdataContent = vm.getUserData();
        if (StringUtils.isEmpty(userdataContent)) {
            vm.setUserData(setPwdScript);
        } else {
            setPwdScript = setPwdScript.replace(BASH_TITLE, "");
            vm.setUserData(userdataContent + setPwdScript);
        }
    }

    private boolean saveBuildVmPackageInfo(String vmId, String id) {
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        if (instantiateInfo == null) {
            return false;
        }
        instantiateInfo.setAppPackageId(id);
        instantiateInfo.setStatus(EnumAppInstantiateStatus.PACKAGE_GENERATE_SUCCESS);
        return vmAppOperationService.updateInstantiateInfo(vmId, instantiateInfo);
    }

}
