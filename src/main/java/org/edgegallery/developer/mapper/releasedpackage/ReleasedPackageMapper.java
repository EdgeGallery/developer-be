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

package org.edgegallery.developer.mapper.releasedpackage;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;

@Mapper
public interface ReleasedPackageMapper {

    int createReleasedPackage(ReleasedPackage releasedPackage);

    ReleasedPackage getReleasedPackageById(@Param("appId") String appId, @Param("packageId") String packageId);

    int deleteReleasedPackageById(@Param("appId") String appId, @Param("packageId") String packageId);

    List<ReleasedPackage> getAllReleasedPackages(@Param("name") String name);

    ReleasedPackage getReleasedPackageByPkgId(@Param("packageId") String packageId);

    int deleteReleasedPackageByPkgId(@Param("packageId") String packageId);

}
