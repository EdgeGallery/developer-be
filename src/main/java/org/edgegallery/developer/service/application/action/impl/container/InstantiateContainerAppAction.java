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

import java.util.Map;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.service.application.action.impl.InstantiateAppAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class InstantiateContainerAppAction extends InstantiateAppAction {

    @Autowired
    private ContainerAppOperationServiceImpl containerAppOperationService;

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        return containerAppOperationService.updateInstantiateInfo(appInstanceId, instantiateInfo);
    }

    public boolean saveWorkloadToInstantiateInfo(String respBody) {
        return true;
    }

    public Map<String, String> getInputParams(MepHost mepHost) {
        return null;
    }
}
