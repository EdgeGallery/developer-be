/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.service.recource.pkgspec;

import java.util.List;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;

public interface PkgSpecService {

    /**
     * get all vm package specifications.
     *
     * @return
     */
    List<PkgSpec> getPkgSpecs();

    /**
     * get vm package specification by id.
     *
     * @param pkgSpecId pkgSpecId
     * @return
     */
    PkgSpec getPkgSpecById(String pkgSpecId);

    /**
     * get vm network by specification id.
     *
     * @param pkgSpecId pkgSpecId
     * @return
     */
    List<Network> getNetworkResourceByPkgSpecId(String pkgSpecId);

    /**
     * get vm specification use scenes.
     *
     * @return
     */
    String getUseScenes();
}
