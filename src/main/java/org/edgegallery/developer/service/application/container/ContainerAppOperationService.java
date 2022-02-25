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

package org.edgegallery.developer.service.application.container;

import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.restful.OperationInfoRep;

public interface ContainerAppOperationService {

    /**
     * instantiate container application.
     *
     * @param applicationId applicationId
     * @param user operator
     * @return
     */
    OperationInfoRep instantiateContainerApp(String applicationId, User user);

    /**
     * get container application instantiate info.
     *
     * @param applicationId applicationId
     * @return
     */
    ContainerAppInstantiateInfo getInstantiateInfo(String applicationId);

    /**
     * save container application instantiate info.
     *
     * @param applicationId applicationId
     * @param instantiateInfo instantiateInfo
     * @return
     */
    Boolean createContainerAppInstantiateInfo(String applicationId, ContainerAppInstantiateInfo instantiateInfo);

    /**
     * update container application instantiate info.
     *
     * @param applicationId applicationId
     * @param instantiateInfo instantiateInfo
     * @return
     */
    Boolean updateInstantiateInfo(String applicationId, ContainerAppInstantiateInfo instantiateInfo);

    /**
     * delete container application instantiate info.
     *
     * @param applicationId applicationId
     * @return
     */
    Boolean deleteInstantiateInfo(String applicationId);

    /**
     * generate csar package(when deploy and test application).
     *
     * @param application container application
     * @return
     */
    AppPackage generatePackage(ContainerApplication application);
}
