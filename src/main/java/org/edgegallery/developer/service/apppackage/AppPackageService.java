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

import java.util.List;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;

public interface AppPackageService {

    /**
     * query app package by id.
     *
     * @param packageId package id
     * @return return app package object or null
     */
    AppPackage getAppPackage(String packageId);

    /**
     * query app package by application id.
     *
     * @param applicationId application id
     * @return return app package object or null
     */
    AppPackage getAppPackageByAppId(String applicationId);

    /**
     * get app package structure by package id.
     *
     * @param packageId package id
     * @return return app package list
     */
    List<AppPkgFile> getAppPackageStructure(String packageId);

    /**
     * get file(under package) content.
     *
     * @param structureReqDto body param(inner file path)
     * @param packageId package id
     * @return return file path and content
     */
    ReleasedPkgFileContent getAppPackageFileContent(ReleasedPkgFileContentReqDto structureReqDto, String packageId);

    /**
     * edit file(under package) content.
     *
     * @param releasedPkgFileContent body param(inner file path and input content)
     * @param packageId package id
     * @return return file path and content
     */
    ReleasedPkgFileContent updateAppPackageFileContent(ReleasedPkgFileContent releasedPkgFileContent, String packageId);

    /**
     * generate app package by vm application.
     *
     * @param application vm application
     * @return return app package object or null
     */
    AppPackage generateAppPackage(VMApplication application);

    /**
     * generate app package by container application.
     *
     * @param application container application
     * @return return app package object or null
     */
    AppPackage generateAppPackage(ContainerApplication application);

    /**
     * delete app package by package id.
     *
     * @param packageId package id
     * @return return true or false
     */
    boolean deletePackage(String packageId);

    /**
     * re-package by package id.
     *
     * @param packageId package id
     * @return return app package object or null
     */
    AppPackage zipPackage(String packageId);

    /**
     * create app package.
     *
     * @param appPackage package object
     * @return return true or false
     */
    boolean createPackage(AppPackage appPackage);
}
