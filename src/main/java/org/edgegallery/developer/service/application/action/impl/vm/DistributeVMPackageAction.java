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
package org.edgegallery.developer.service.application.action.impl.vm;

import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.service.application.action.impl.DistributePackageAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.util.SpringContextUtil;

public class DistributeVMPackageAction extends DistributePackageAction {

    VMAppOperationServiceImpl vmAppOperationService = (VMAppOperationServiceImpl) SpringContextUtil.getBean(VMAppOperationServiceImpl.class);

    @Override
    public boolean saveDistributeInstantiateInfo(String mecHostIp, String mepmPkgId, EnumAppInstantiateStatus status){
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        instantiateInfo.setDistributedMecHost(mecHostIp);
        instantiateInfo.setMepmPackageId(mepmPkgId);
        instantiateInfo.setStatus(status);
        return  vmAppOperationService.updateInstantiateInfo(vmId, instantiateInfo);
    }

}
