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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.application.vm.EnumVMStatus;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.filesystem.FileSystemResponse;
import org.edgegallery.developer.model.instantiate.vm.EnumImageExportStatus;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.lcm.LcmLog;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.resource.vm.EnumVmImageSlimStatus;
import org.edgegallery.developer.model.resource.vm.EnumVmImageStatus;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.recource.vm.VMImageService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadImageAction extends AbstractAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(DownloadImageAction.class);

    public static final String ACTION_NAME = "Download Image";

    // time out: 120 min.
    public static final int TIMEOUT = 120 * 60 * 1000;

    //interval of the query, 20s.
    public static final int INTERVAL = 20000;

    VMAppOperationServiceImpl vmAppOperationService = (VMAppOperationServiceImpl) SpringContextUtil
        .getBean(VMAppOperationServiceImpl.class);

    VMImageService vmImageService = (VMImageService) SpringContextUtil.getBean(VMImageService.class);

    VMAppVmService vmAppVmService = (VMAppVmService) SpringContextUtil.getBean(VMAppVmService.class);

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        //Start action , save action status.
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        String statusLog = "Start to download vm image for vm Id：" + vmId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.VM_IMAGE_INSTANCE, vmId, ACTION_NAME,
            statusLog);
        //create image.
        updateActionProgress(actionStatus, 30, "start to query image info");
        LcmLog lcmLog = new LcmLog();
        boolean result = queryImageInfoFromFileSystem();
        if (!result) {
            String msg = "query vm  image info from fileSystem failed. The log is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
            modifyImageExportInfo(EnumImageExportStatus.FAILED, msg);
        }
        String msg = "query vm  image info from fileSystem success";
        updateActionProgress(actionStatus, 60, msg);
        modifyImageExportInfo(EnumImageExportStatus.SUCCESS, msg);
        // save to image mgmt
        boolean saveResult = saveImageToImageMgmt();
        if (!saveResult) {
            updateActionError(actionStatus, "save vm  image info to imageMgmt fail.");
        }
        updateActionProgress(actionStatus, 100, "download image success");
        return true;
    }

    private boolean saveImageToImageMgmt() {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VirtualMachine vm = vmAppVmService.getVm(applicationId, vmId);
        VMImage vmImage = vmImageService.getVmImageById(vm.getImageId());
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmm");
        vmImage.setName(vm.getImageExportInfo().getName() + "-" + date.format(new Date()));
        vmImage.setDownLoadUrl(vm.getImageExportInfo().getDownloadUrl());
        vmImage.setFileMd5(vm.getImageExportInfo().getCheckSum());
        vmImage.setImageFileName(vm.getImageExportInfo().getImageFileName());
        vmImage.setImageSize(Long.valueOf(vm.getImageExportInfo().getImageSize()));
        vmImage.setImageFormat(vm.getImageExportInfo().getFormat());
        vmImage.setImageSlimStatus(EnumVmImageSlimStatus.SLIM_SUCCEED);
        vmImage.setStatus(EnumVmImageStatus.PUBLISHED);
        vmImage.setVirtualSize(vmImage.getSystemDiskSize());
        vmImage.setVisibleType("private");
        VMImage res = vmImageService.createVmImageAllInfo(vmImage);
        if (res == null) {
            LOGGER.error("save image info to imageMgmt fail.");
            return false;
        }
        vmAppVmService.updateVmStatus(vmId, EnumVMStatus.EXPORTED, res.getId());
        return true;

    }

    private boolean queryImageInfoFromFileSystem() {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = vmAppOperationService.getImageExportInfo(vmId);
        int waitingTime = 0;
        // try to 3 time if return null
        int failNum = 0;
        String url = imageExportInfo.getDownloadUrl();
        while (waitingTime < TIMEOUT) {
            FileSystemResponse imageResult = HttpClientUtil.queryImageCheck(url);
            if (imageResult == null) {
                failNum++;
            } else {
                String checkSum = imageResult.getCheckStatusResponse().getCheckInfo().getChecksum();
                if (!StringUtils.isEmpty(checkSum)) {
                    imageExportInfo.setCheckSum(checkSum);
                    imageExportInfo.setImageFileName(imageResult.getFileName());
                    imageExportInfo.setStatus(EnumImageExportStatus.SUCCESS);
                    imageExportInfo
                        .setFormat(imageResult.getCheckStatusResponse().getCheckInfo().getImageInfo().getFormat());
                    imageExportInfo.setImageSize(
                        imageResult.getCheckStatusResponse().getCheckInfo().getImageInfo().getImageSize());
                    imageExportInfo.setDownloadUrl(url + "/action/download");
                    vmAppOperationService.modifyExportInfo(vmId, imageExportInfo);
                    return true;
                }
            }
            if (failNum >= 3) {
                return false;
            }
            try {
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.error("export image sleep failed.");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private Boolean modifyImageExportInfo(EnumImageExportStatus status, String log) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = vmAppOperationService.getImageExportInfo(vmId);
        imageExportInfo.setStatus(status);
        imageExportInfo.setLog(log);
        vmAppOperationService.modifyExportInfo(vmId, imageExportInfo);
        return true;
    }
}
