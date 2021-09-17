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

import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("OperationService")
public class AppPackageServiceImpl implements AppPackageService {

    @Override
    public AppPackage getAppPackage(String packageId) {
        return null;
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
}
