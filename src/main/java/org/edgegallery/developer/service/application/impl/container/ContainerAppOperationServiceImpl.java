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

package org.edgegallery.developer.service.application.impl.container;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.application.container.HelmChartMapper;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.container.ContainerLaunchOperation;
import org.edgegallery.developer.service.application.action.impl.vm.VMLaunchOperation;
import org.edgegallery.developer.service.application.container.ContainerAppOperationService;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("containerAppActionService")
public class ContainerAppOperationServiceImpl extends AppOperationServiceImpl implements ContainerAppOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppOperationServiceImpl.class);

    private static final String OPERATION_NAME = "ContainerApp launch";

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private HelmChartMapper helmChartMapper;

    @Autowired
    private OperationStatusMapper operationStatusMapper;

    @Override
    public OperationInfoRep instantiateContainerApp(String applicationId, String helmChartId, String accessToken) {
        Application application = applicationMapper.getApplicationById(applicationId);
        if (application == null) {
            LOGGER.error("application is not exited,id:{}", applicationId);
            throw new EntityNotFoundException("application is not exited.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        HelmChart helmChart = helmChartMapper.getHelmChartById(helmChartId);
        if (helmChart == null || StringUtils.isEmpty(helmChart.getHelmChartFileId())) {
            LOGGER.error("instantiate container app fail ,helmchart file id not exist,vmId:{}", helmChartId);
            throw new EntityNotFoundException("instantiate container app fail,helmchart file id not exist.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setUserName(AccessUserUtil.getUser().getUserName());
        operationStatus.setObjectType(EnumOperationObjectType.APPLICATION);
        operationStatus.setObjectId(applicationId);
        operationStatus.setObjectName(application.getName());
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setOperationName(OPERATION_NAME);
        int res = operationStatusMapper.createOperationStatus(operationStatus);
        if (res < 1) {
            LOGGER.error("Create operationStatus in db error.");
            throw new DataBaseException("Create operationStatus in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        ContainerLaunchOperation actionCollection = new ContainerLaunchOperation(AccessUserUtil.getUser(), applicationId, helmChartId,
            accessToken, operationStatus);
        LOGGER.info("start instantiate container app");
        new InstantiateContainerAppProcessor(actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }
    public static class InstantiateContainerAppProcessor extends Thread {

        ContainerLaunchOperation actionCollection;

        public InstantiateContainerAppProcessor(ContainerLaunchOperation actionCollection) {
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            IActionIterator iterator = actionCollection.getActionIterator();
            while (iterator.hasNext()) {
                IAction action = iterator.nextAction();
                boolean result = action.execute();
                if (!result) {
                    break;
                }
            }
        }
    }
}
