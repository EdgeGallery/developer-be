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

package org.edgegallery.developer.service.application.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.application.container.HelmChartMapper;
import org.edgegallery.developer.mapper.application.vm.NetworkMapper;
import org.edgegallery.developer.mapper.application.vm.VMMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.uploadfile.UploadService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("applicationService")
public class ApplicationServiceImpl implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private VMMapper vmMapper;

    @Autowired
    private HelmChartMapper helmChartMapper;

    @Autowired
    AppConfigurationService appConfigurationService;

    @Override
    public Application createApplication(Application application) {
        String applicationId = UUID.randomUUID().toString();
        String applicationPath = DeveloperFileUtils.getAbsolutePath(applicationId);
        try {
            DeveloperFileUtils.deleteAndCreateDir(applicationPath);
        } catch (IOException e1) {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Create application work path failed.");
            throw new DeveloperException("Create application work path failed.", ResponseConsts.APPLICATION_DIR_CREATE_FAILED);
        }
        application.setId(applicationId);
        application.setUserId(AccessUserUtil.getUser().getUserId());
        application.setUserName(AccessUserUtil.getUser().getUserName());
        String iconFileId = application.getIconFileId();
        application.setStatus(EnumApplicationStatus.ONLINE);
        if (iconFileId == null) {
            LOGGER.error("icon file is null");
            throw new DeveloperException("icon file is null", ResponseConsts.ICON_FILE_NULL);
        }
        uploadService.moveFileToWorkSpaceById(iconFileId, applicationId);
        // init network
        initNetwork(applicationId);

        // save application to DB
        int res = applicationMapper.createApplication(application);
        if (res < 1) {
            LOGGER.error("Create application in db error.");
            throw new DeveloperException("Create application in db error.", ResponseConsts.INSERT_DATA_FAILED);
        }
        LOGGER.info("Create application success.");
        return application;
    }

    private void initNetwork(String applicationId) {
        List<Network> networks = networkMapper.getNetworkByAppId("init-application");
        for (Network network : networks) {
            network.setId(UUID.randomUUID().toString());
            int res = networkMapper.createNetwork(applicationId, network);
            if (res < 1) {
                LOGGER.error("Create network in db error.");
                throw new DeveloperException("Create network  in db error.", ResponseConsts.INSERT_DATA_FAILED);
            }
        }
    }

    @Override
    public Application getApplication(String applicationId) {
        return applicationMapper.getApplicationById(applicationId);
    }

    @Override
    public Boolean modifyApplication(String applicationId, Application application) {
        int res = applicationMapper.modifyApplication(application);
        if (res < 1) {
            LOGGER.error("modify application in db error.");
            throw new DeveloperException("modify application in db error.", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return true;
    }

    @Override
    public Page<Application> getApplicationByNameWithFuzzy(String appName, int limit, int offset) {
        String userId = AccessUserUtil.getUser().getUserId();
        PageHelper.offsetPage(offset, limit);
        PageInfo<Application> pageInfo = new PageInfo<Application>(
            applicationMapper.getAllApplicationsByUserId(userId, appName));
        LOGGER.info("get all applications success.");
        return new Page<Application>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    @Override
    public Boolean deleteApplication(String applicationId) {
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("Can not find application by applicationId:{}.", applicationId);
            throw new DeveloperException("Application does not exist.", ResponseConsts.DATA_NOT_EXIST);
        }
        // delete the application from db
        int delResult = applicationMapper.deleteApplication(applicationId);
        if (delResult < 1) {
            LOGGER.error("Delete application by applicationId:{} failed.", applicationId);
            throw new DeveloperException("Delete application failed.", ResponseConsts.DELETE_DATA_FAILED);
        }
        // delete files of application
        String applicationPath = DeveloperFileUtils.getAbsolutePath(applicationId);
        DeveloperFileUtils.deleteDir(applicationPath);
        LOGGER.info("Delete application {} success.", applicationId);
        return true;
    }

    @Override
    public ApplicationDetail getApplicationDetail(String applicationId) {
        ApplicationDetail applicationDetail = new ApplicationDetail();
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("Can not find application by applicationId:{}.", applicationId);
            throw new DeveloperException("Application does not exist.", ResponseConsts.DATA_NOT_EXIST);
        }
        if (application.getAppClass() == EnumAppClass.VM) {
            VMApplication vmApplication = new VMApplication(application);

            vmApplication.setNetworkList(networkMapper.getNetworkByAppId(applicationId));
            vmApplication.setVmList(vmMapper.getAllVMsByAppId(applicationId));

            vmApplication.setAppConfiguration(appConfigurationService.getAppConfiguration(applicationId));
            applicationDetail.setVmApp(vmApplication);
        } else {
            ContainerApplication containerApplication = new ContainerApplication(application);
            containerApplication.setHelmChartList(helmChartMapper.getHelmChartsByAppId(applicationId));
            containerApplication.setAppConfiguration(appConfigurationService.getAppConfiguration(applicationId));
            applicationDetail.setContainerApp(containerApplication);
        }
        return applicationDetail;
    }

    @Override
    public Boolean modifyApplicationDetail(String applicationId, ApplicationDetail applicationDetail) {
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("Can not find application by applicationId:{}.", applicationId);
            throw new DeveloperException("Application does not exist.", ResponseConsts.DATA_NOT_EXIST);
        }
        if (application.getAppClass() == EnumAppClass.VM) {
            applicationMapper.modifyApplication(applicationDetail.getVmApp());
            appConfigurationService.modifyAppConfiguration(applicationId,
                applicationDetail.getVmApp().getAppConfiguration());
            for (Network network : applicationDetail.getVmApp().getNetworkList()) {
                networkMapper.modifyNetwork(network);
            }
            for (VirtualMachine virtualMachine : applicationDetail.getVmApp().getVmList()) {
                vmMapper.modifyVM(virtualMachine);
            }
        } else {
            applicationMapper.modifyApplication(applicationDetail.getContainerApp());
            appConfigurationService.modifyAppConfiguration(applicationId,
                applicationDetail.getContainerApp().getAppConfiguration());
            //todo modify helmchart
        }
        return true;

    }

    @Override
    public UploadedFile uploadIconFile(MultipartFile uploadFile) {
        LOGGER.info("Start uploading file");
        String fileName = uploadFile.getOriginalFilename();
        String userId = AccessUserUtil.getUser().getUserId();
        Boolean iconTypeCheck = iconTypeCheck(fileName);
        if (!iconTypeCheck) {
            LOGGER.error("File type is error.");
            throw new DeveloperException("File type is error.", ResponseConsts.DELETE_DATA_FAILED);
        }

        UploadedFile result = uploadService.saveFileToLocal(uploadFile, userId);
        if (result == null) {
            LOGGER.error("File type is error.");
            throw new DeveloperException("File type is error.", ResponseConsts.DELETE_DATA_FAILED);
        }
        LOGGER.error("File type is error.");
        throw new DeveloperException("File type is error.", ResponseConsts.DELETE_DATA_FAILED);
    }

    private Boolean iconTypeCheck(String fileName) {
        if (!FileChecker.isValid(fileName)) {
            LOGGER.error("File Name is invalid.");
            return false;
        }
        String fileType = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileType = fileName.substring(i + 1);
        }
        if (!"jpg".equals(fileType) && !"png".equals(fileType)) {
            LOGGER.error("File type is error.");
            return false;
        }
        return true;
    }

}
