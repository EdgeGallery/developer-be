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

import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.filesystem.FileSystemResponse;
import org.edgegallery.developer.model.instantiate.vm.EnumImageExportStatus;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class DownloadImageAction extends AbstractAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(DownloadImageAction.class);

    private static Gson gson = new Gson();

    public static final String ACTION_NAME = "Download Image";

    // time out: 10 min.
    public static final int TIMEOUT = 10 * 60 * 1000;
    //interval of the query, 5s.
    public static final int INTERVAL = 5000;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    ImageExportInfoMapper imageExportInfoMapper;

    private IContext context;

    public IContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(IContext context) {
        this.context = context;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        //Start action , save action status.
        String packageId = (String) getContext().getParameter(IContextParameter.PARAM_PACKAGE_ID);
        String statusLog = "Start to create vm image for package Id：" + packageId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.VM_IMAGE_DOWNLOAD, packageId,
            ACTION_NAME, statusLog);
        //create image.
        updateActionProgress(actionStatus, 30, "start to query image info");
        LcmLog lcmLog = new LcmLog();
        boolean result = queryImageInfoFromFileSystem();
        if (!result) {
            String msg = "query vm  image info from fileSystem failed. The log is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
        }
        String msg = "query vm  image info from fileSystem success";
        updateActionProgress(actionStatus, 100, msg);
        return true;
    }

    private boolean queryImageInfoFromFileSystem() {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = new ImageExportInfo();
        int waitingTime = 0;
        String url = (String) getContext().getParameter(IContextParameter.PARAM_IMAGE_DOWNLOAD_URL);
        while (waitingTime < TIMEOUT) {
            String slimResult = HttpClientUtil.getImageSlim(url);
            FileSystemResponse imageResult;
            if (slimResult==null) {
                return false;
            }
            try {
                imageResult = new ObjectMapper().readValue(slimResult.getBytes(), FileSystemResponse.class);
                String checkSum = imageResult.getCheckStatusResponse().getCheckInfo().getChecksum();
                if (!StringUtils.isEmpty(checkSum)) {
                    imageExportInfo.setImageInstanceId(imageResult.getImageId());
                    imageExportInfo.setCheckSum(checkSum);
                    imageExportInfo.setImageName(imageResult.getFileName());
                    imageExportInfo.setStatus(EnumImageExportStatus.SUCCESS);
                    imageExportInfo.setFormat(imageResult.getCheckStatusResponse().getCheckInfo().getImageInfo().getFormat());
                    imageExportInfo.setDownloadUrl(url + "action/download");
                    imageExportInfoMapper.modifyImageExportInfoInfoByVMId(vmId, imageExportInfo);
                    return true;
                }
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.error("export image sleep failed.");
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
