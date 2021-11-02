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
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.application.factory.AppOperationServiceFactory;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.uploadfile.UploadService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("applicationService")
public class ApplicationServiceImpl implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private VMAppNetworkService networkService;

    @Autowired
    private VMAppVmService vmService;

    @Autowired
    private ContainerAppHelmChartService helmChartService;

    @Autowired
    AppConfigurationService appConfigurationService;

    @Autowired
    private AppOperationServiceFactory appServiceFactory;

    @Override
    public Application createApplication(Application application) {
        String applicationId = UUID.randomUUID().toString();
        String applicationPath = DeveloperFileUtils.getAbsolutePath(applicationId);
        try {
            DeveloperFileUtils.deleteAndCreateDir(applicationPath);
        } catch (IOException e1) {
            throw new FileOperateException("Create application work path failed.", ResponseConsts.RET_CREATE_FILE_FAIL);
        }
        application.setId(applicationId);
        application.setUserId(AccessUserUtil.getUser().getUserId());
        application.setUserName(AccessUserUtil.getUser().getUserName());
        String iconFileId = application.getIconFileId();
        application.setStatus(EnumApplicationStatus.CREATED);
        if (iconFileId == null) {
            LOGGER.error("icon file is null");
            throw new FileFoundFailException("icon file is null", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        uploadService.moveFileToWorkSpaceById(iconFileId, applicationId);
        if(application.getAppClass().equals(EnumAppClass.VM)){
            // init VM application default networks
            initNetwork(applicationId);
        }

        // save application to DB
        int res = applicationMapper.createApplication(application);
        if (res < 1) {
            LOGGER.error("Create application in db error.");
            throw new DataBaseException("Create application in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        LOGGER.info("Create application success.");
        //query db to get model with default values.
        Application app = applicationMapper.getApplicationById(applicationId);
        return app;
    }

    private void initNetwork(String applicationId) {
        List<Network> networks = networkService.getAllNetwork("init-application");
        for (Network network : networks) {
            network.setId(UUID.randomUUID().toString());
            networkService.createNetwork(applicationId, network);
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
            throw new DataBaseException("modify application in db error.", ResponseConsts.RET_UPDATE_DATA_FAIL);
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
    public Boolean deleteApplication(String applicationId, User user) {
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("Can not find application by applicationId:{}.", applicationId);
            throw new EntityNotFoundException("Application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        // clean env
        appServiceFactory.getAppOperationService(applicationId).cleanEnv(applicationId, user);

        // delete the application from db
        int delResult = applicationMapper.deleteApplication(applicationId);
        if (delResult < 1) {
            LOGGER.error("Delete application by applicationId:{} failed.", applicationId);
            throw new DataBaseException("Delete application failed.", ResponseConsts.RET_DELETE_DATA_FAIL);
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
            throw new EntityNotFoundException("Application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (application.getAppClass() == EnumAppClass.VM) {
            VMApplication vmApplication = new VMApplication(application);

            vmApplication.setNetworkList(networkService.getAllNetwork(applicationId));
            vmApplication.setVmList(vmService.getAllVm(applicationId));
            vmApplication.setAppConfiguration(appConfigurationService.getAppConfiguration(applicationId));
            applicationDetail.setVmApp(vmApplication);
        } else {
            ContainerApplication containerApplication = new ContainerApplication(application);
            containerApplication.setHelmChartList(helmChartService.getHelmChartList(applicationId));
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
            throw new EntityNotFoundException("Application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (application.getAppClass() == EnumAppClass.VM) {
            applicationMapper.modifyApplication(applicationDetail.getVmApp());
            appConfigurationService
                .modifyAppConfiguration(applicationId, applicationDetail.getVmApp().getAppConfiguration());
            for (Network network : applicationDetail.getVmApp().getNetworkList()) {
                networkService.modifyNetwork(applicationId, network.getId(), network);
            }
            for (VirtualMachine virtualMachine : applicationDetail.getVmApp().getVmList()) {
                vmService.modifyVm(applicationId, virtualMachine.getId(), virtualMachine);
            }
        } else {
            applicationMapper.modifyApplication(applicationDetail.getContainerApp());
            appConfigurationService
                .modifyAppConfiguration(applicationId, applicationDetail.getContainerApp().getAppConfiguration());
            //todo modify helmchart
        }
        return true;

    }

    @Override
    public Boolean updateApplicationStatus(String applicationId, EnumApplicationStatus status) {
        int res = applicationMapper.updateApplicationStatus(applicationId, status.toString());
        if (res < 1) {
            LOGGER.error("update application status by applicationId:{} failed.", applicationId);
            throw new DataBaseException("update application status failed.", ResponseConsts.RET_DELETE_DATA_FAIL);
        }
        return true;
    }

}
