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

package org.edgegallery.developer.service.apppackage.impl;

import java.util.UUID;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.mapper.apppackage.AppPackageMapper;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.apppackage.csar.VMPackageFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppPackageServiceImpl implements AppPackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPackageServiceImpl.class);

    @Autowired
    AppPackageMapper appPackageMapper;

    @Override
    public AppPackage getAppPackage(String packageId) {
        return appPackageMapper.getAppPackage(packageId);
    }

    @Override
    public AppPkgStructure getAppPackageStructure(String packageId) {
        return null;
    }

    @Override
    public String getAppPackageFileContent(String packageId, String structureItemId) {
        return null;
    }

    @Override
    public boolean updateAppPackageFileContent(String packageId, String structureItemId, String content) {
        return false;
    }

    @Override
    public AppPackage generateAppPackage(VMApplication application) {
        AppPackage appPackage = new AppPackage();
        appPackage.setId(UUID.randomUUID().toString());
        appPackage.setAppId(application.getId());
        try {
            // generation appd
            VMPackageFileCreator vmPackageFileCreator = new VMPackageFileCreator(application, appPackage.getId());
            String fileName = vmPackageFileCreator.generateAppPackageFile();
            // generation scar
        } catch (Exception e) {
            LOGGER.error("Generation app package error.");
            throw new FileOperateException("Generation app package error.", ResponseConsts.RET_CREATE_FILE_FAIL);
        }
        return appPackage;
    }

    @Override
    public AppPackage generateAppPackage(ContainerApplication application) {
        return null;
    }
}
