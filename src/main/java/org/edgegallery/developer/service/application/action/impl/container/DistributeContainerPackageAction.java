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

package org.edgegallery.developer.service.application.action.impl.container;

import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.service.application.action.impl.DistributePackageAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.edgegallery.developer.util.SpringContextUtil;

public class DistributeContainerPackageAction extends DistributePackageAction {

    ContainerAppOperationServiceImpl containerAppOperationService = (ContainerAppOperationServiceImpl) SpringContextUtil
        .getBean(ContainerAppOperationServiceImpl.class);

    public boolean saveDistributeInstantiateInfo(String mecHostIp, String mepmPkgId, EnumAppInstantiateStatus status) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        instantiateInfo.setDistributedMecHost(mecHostIp);
        instantiateInfo.setMepmPackageId(mepmPkgId);
        instantiateInfo.setStatus(status);
        return containerAppOperationService.updateInstantiateInfo(applicationId, instantiateInfo);
    }

}
