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
package org.edgegallery.developer.service.application.factory;

import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.service.application.AppOperationService;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AppOperationServiceFactory")
public class AppOperationServiceFactory {

    @Autowired
    private ApplicationMapper appMapper;

    @Autowired
    private VMAppOperationServiceImpl vmAppOperationService;

    @Autowired
    private ContainerAppOperationServiceImpl containerAppOperationService;

    public AppOperationService getAppOperationService(String applicationId) {
        Application app = appMapper.getApplicationById(applicationId);
        return getAppOperationService(app.getAppClass());
    }

    private AppOperationService getAppOperationService(EnumAppClass appClass) {
        if (EnumAppClass.CONTAINER.equals(appClass)) {
            return containerAppOperationService;
        } else if (EnumAppClass.VM.equals(appClass)) {
            return vmAppOperationService;
        }
        return null;
    }
}
