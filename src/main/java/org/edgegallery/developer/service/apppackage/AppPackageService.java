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

package org.edgegallery.developer.service.apppackage;

import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;

public interface AppPackageService {

    AppPackage getAppPackage(String packageId);

    AppPkgStructure getAppPackageStructure(String packageId);

    String getAppPackageFileContent(String packageId, String fileName);

    boolean updateAppPackageFileContent(String packageId, String fileName, String content);

    AppPackage generateAppPackage(VMApplication application);

    AppPackage generateAppPackage(ContainerApplication application);

    boolean deletePackage(String packageId);
}