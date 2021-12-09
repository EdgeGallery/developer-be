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

    boolean synchronizePackage(User user, List<ReleasedPkgReqDto> pkgReqDtos);

    Page<ReleasedPackage> getAllPackages(String name, int limit, int offset);

    List<AppPkgFile> getAppPkgStructure(String packageId);

    ReleasedPkgFileContent getAppPkgFileContent(ReleasedPkgFileContentReqDto structureReqDto, String packageId);

    ReleasedPkgFileContent editAppPkgFileContent(ReleasedPkgFileContent releasedPkgFileContent, String packageId);

    boolean deleteAppPkg(String packageId);

    boolean releaseAppPkg(User user, PublishAppReqDto publishAppReqDto, String packageId);

}
