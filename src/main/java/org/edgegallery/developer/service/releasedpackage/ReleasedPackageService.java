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

package org.edgegallery.developer.service.releasedpackage;

import java.util.List;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgReqDto;

public interface ReleasedPackageService {

    /**
     * synchronize released app package.
     *
     * @param user current login user
     * @param pkgReqDtos body param
     * @return if success return true or false
     */
    boolean synchronizePackage(User user, List<ReleasedPkgReqDto> pkgReqDtos);

    /**
     * get all synchronize package.
     *
     * @param name filter condition
     * @param limit page limit
     * @param offset page offset
     * @return if success return paging data or throw exception
     */
    Page<ReleasedPackage> getAllPackages(String name, int limit, int offset);

    /**
     * get app package structure.
     *
     * @param packageId package id
     * @return return AppPkgFile list
     */
    List<AppPkgFile> getAppPkgStructure(String packageId);

    /**
     * get file(under package) content.
     *
     * @param structureReqDto body param(inner file path)
     * @param packageId package id
     * @return return file path and content
     */
    ReleasedPkgFileContent getAppPkgFileContent(ReleasedPkgFileContentReqDto structureReqDto, String packageId);

    /**
     * get file(under package) content.
     *
     * @param releasedPkgFileContent body param(inner file path and content)
     * @param packageId package id
     * @return return file path and content
     */
    ReleasedPkgFileContent editAppPkgFileContent(ReleasedPkgFileContent releasedPkgFileContent, String packageId);

    /**
     * delete app package.
     *
     * @param packageId package id
     * @return if success return true or return false
     */
    boolean deleteAppPkg(String packageId);

    /**
     * release app package.
     *
     * @param user current login user info
     * @param publishAppReqDto body param(is free and price)
     * @param packageId package id
     * @return if success return true or return false
     */
    boolean releaseAppPkg(User user, PublishAppReqDto publishAppReqDto, String packageId);

}
