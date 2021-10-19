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

package org.edgegallery.developer.service.application.impl;

import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.restful.SelectMepHostReq;
import org.edgegallery.developer.service.application.AppOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AppOperationService")
public class AppOperationServiceImpl implements AppOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppOperationServiceImpl.class);

    @Autowired
    private ApplicationMapper applicationMapper;

    @Override
    public Boolean cleanEnv(String applicationId) {
        return null;
    }

    @Override
    public AppPackage generatePackage(String applicationId) {
        return null;
    }

    @Override
    public Boolean commitTest(String applicationId) {
        return null;
    }

    @Override
    public Boolean selectMepHost(String applicationId, SelectMepHostReq selectMepHostReq) {
        int res = applicationMapper.modifyMepHostById(applicationId, selectMepHostReq.getMepHostId());
        if (res < 1) {
            LOGGER.error("modify mep host  of application {} fail", applicationId);
            throw new DataBaseException("modify mep host of application fail", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }
}
