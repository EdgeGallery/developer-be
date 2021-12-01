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

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.application.container.ContainerAppInstantiateInfoMapper;
import org.edgegallery.developer.mapper.application.container.HelmChartMapper;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.mapper.resource.mephost.MepHostMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.instantiate.container.Container;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.instantiate.container.K8sPod;
import org.edgegallery.developer.model.instantiate.container.K8sService;
import org.edgegallery.developer.model.instantiate.container.K8sServicePort;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.container.ContainerLaunchOperation;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.application.container.ContainerAppOperationService;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("containerAppActionService")
public class ContainerAppOperationServiceImpl extends AppOperationServiceImpl implements ContainerAppOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppOperationServiceImpl.class);

    private static final String OPERATION_NAME = "ContainerApp launch";

    @Autowired
    ApplicationService applicationService;

    @Autowired
    private ContainerAppHelmChartService containerAppHelmChartService;

    @Autowired
    private OperationStatusService operationStatusService;

    @Autowired
    private ContainerAppInstantiateInfoMapper containerAppInstantiateInfoMapper;

    @Autowired
    private AppPackageService appPackageService;

    @Autowired
    private MepHostService mepHostService;

    public AppPackage generatePackage(String applicationId) {
        ApplicationDetail detail = applicationService.getApplicationDetail(applicationId);
        return generatePackage(detail.getContainerApp());
    }

    public AppPackage generatePackage(ContainerApplication application) {
        return appPackageService.generateAppPackage(application);
    }

    @Override
    public OperationInfoRep instantiateContainerApp(String applicationId, User user) {
        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application does not exist,id:{}", applicationId);
            throw new EntityNotFoundException("application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        List<HelmChart> helmCharts = containerAppHelmChartService.getHelmChartList(applicationId);
        if (CollectionUtils.isEmpty(helmCharts)) {
            LOGGER.error("instantiate container app fail ,helmchart file  not exist,applicationId:{}", applicationId);
            throw new EntityNotFoundException("instantiate container app fail,helmchart file id not exist.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (containerAppInstantiateInfoMapper.getContainerAppInstantiateInfoAppId(applicationId) != null) {
            LOGGER.error("Container application has already been instantiated.,applicationId:{}", applicationId);
            throw new EntityNotFoundException("Container application has already been instantiated.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setUserName(user.getUserName());
        operationStatus.setObjectType(EnumOperationObjectType.APPLICATION_INSTANCE);
        operationStatus.setObjectId(applicationId);
        operationStatus.setObjectName(application.getName());
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setOperationName(OPERATION_NAME);
        operationStatusService.createOperationStatus(operationStatus);
        ContainerLaunchOperation actionCollection = new ContainerLaunchOperation(user,
            applicationId, operationStatus);
        LOGGER.info("start instantiate container app");
        ContainerAppInstantiateInfo containerAppInstantiateInfo = new ContainerAppInstantiateInfo();
        containerAppInstantiateInfo.setOperationId(operationStatus.getId());
        createContainerAppInstantiateInfo(applicationId, containerAppInstantiateInfo);
        new InstantiateContainerAppProcessor(operationStatusService, operationStatus, actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }

    @Override
    public Boolean cleanEnv(String applicationId, User user) {
        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application does not exist ,id:{}", applicationId);
            throw new EntityNotFoundException("application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (StringUtils.isEmpty(application.getMepHostId())) {
            return true;
        }

        ContainerAppInstantiateInfo containerAppInstantiateInfo = getInstantiateInfo(applicationId);
        if (containerAppInstantiateInfo != null) {
            cleanContainerLaunchInfo(application.getMepHostId(), containerAppInstantiateInfo, user);
            deleteInstantiateInfo(applicationId);
        }
        return true;
    }

    private boolean cleanContainerLaunchInfo(String mepHostId, ContainerAppInstantiateInfo containerAppInstantiateInfo,
        User user) {
        MepHost mepHost = mepHostService.getHost(mepHostId);
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        if (StringUtils.isNotEmpty(containerAppInstantiateInfo.getMepmPackageId()) || StringUtils
            .isNotEmpty(containerAppInstantiateInfo.getAppInstanceId())) {
            sentTerminateRequestToLcm(basePath, user.getUserId(), user.getToken(),
                containerAppInstantiateInfo.getAppInstanceId(),
                containerAppInstantiateInfo.getMepmPackageId(), mepHost.getMecHostIp());
        }
        return true;
    }

    @Override
    public ContainerAppInstantiateInfo getInstantiateInfo(String applicationId) {
        ContainerAppInstantiateInfo instantiateInfo
            = containerAppInstantiateInfoMapper.getContainerAppInstantiateInfoAppId(applicationId);
        if (instantiateInfo == null) {
            return null;
        }
        List<K8sPod> k8sPods = containerAppInstantiateInfoMapper.getK8sPodsByAppId(applicationId);
        if (!CollectionUtils.isEmpty(k8sPods)) {
            for (K8sPod pod : k8sPods) {
                List<Container> containers = containerAppInstantiateInfoMapper.getContainersByPodName(pod.getName());
                pod.setContainerList(containers);
            }
            instantiateInfo.setPods(k8sPods);
        }
        List<K8sService> k8sServices = containerAppInstantiateInfoMapper.getK8sServiceByAppId(applicationId);
        if (!CollectionUtils.isEmpty(k8sServices)) {
            for (K8sService service : k8sServices) {
                List<K8sServicePort> k8sServicePorts = containerAppInstantiateInfoMapper.getK8sServicePortsByK8sServiceName(
                    service.getName());
                service.setServicePortList(k8sServicePorts);
            }
            instantiateInfo.setServiceList(k8sServices);
        }

        return instantiateInfo;
    }

    @Override
    public Boolean createContainerAppInstantiateInfo(String applicationId, ContainerAppInstantiateInfo instantiateInfo) {
        int res = containerAppInstantiateInfoMapper.createContainerAppInstantiateInfo(applicationId, instantiateInfo);
        if (res<1) {
            LOGGER.error("Create container App instantiate info in db error.");
            throw new DataBaseException("Create container App instantiate info in db error.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean updateInstantiateInfo(String applicationId, ContainerAppInstantiateInfo instantiateInfo) {
        int res = containerAppInstantiateInfoMapper.modifyContainerAppInstantiateInfo(applicationId, instantiateInfo);
        if (res < 1) {
            LOGGER.error("Update container instantiate info failed");
            return false;
        }
        //remove and add container pods and service details
        List<K8sPod> k8sPods = containerAppInstantiateInfoMapper.getK8sPodsByAppId(applicationId);
        if (!CollectionUtils.isEmpty(k8sPods)) {
            for (K8sPod pod : k8sPods) {
                containerAppInstantiateInfoMapper.deleteContainerByPodName(pod.getName());
            }
            containerAppInstantiateInfoMapper.deleteK8sPodByAppId(applicationId);
        }
        List<K8sService> k8sServices = containerAppInstantiateInfoMapper.getK8sServiceByAppId(applicationId);
        if (!CollectionUtils.isEmpty(k8sServices)) {
            for (K8sService service : k8sServices) {
                containerAppInstantiateInfoMapper.deleteK8sServicePortByK8sServiceName(service.getName());
            }
            containerAppInstantiateInfoMapper.deleteK8sServiceByAppId(applicationId);
        }
        if (!CollectionUtils.isEmpty(instantiateInfo.getPods())) {
            for (K8sPod pod : instantiateInfo.getPods()) {
                List<Container> containers = pod.getContainerList();
                for (Container container : containers) {
                    containerAppInstantiateInfoMapper.createContainer(pod.getName(), container);
                }
                containerAppInstantiateInfoMapper.createK8sPod(applicationId, pod);
            }
        }
        if (!CollectionUtils.isEmpty(instantiateInfo.getServiceList())) {
            for (K8sService service : instantiateInfo.getServiceList()) {
                List<K8sServicePort> ports = service.getServicePortList();
                for (K8sServicePort port : ports) {
                    containerAppInstantiateInfoMapper.createK8sServicePort(service.getName(), port);
                }
                containerAppInstantiateInfoMapper.createK8sService(applicationId, service);
            }
        }

        return true;
    }

    @Override
    public Boolean deleteInstantiateInfo(String applicationId) {
        ContainerAppInstantiateInfo containerAppInstantiateInfo = getInstantiateInfo(applicationId);
        if (containerAppInstantiateInfo == null) {
            return true;
        }
        if (!CollectionUtils.isEmpty(containerAppInstantiateInfo.getPods())) {
            for (K8sPod k8sPod : containerAppInstantiateInfo.getPods()) {
                containerAppInstantiateInfoMapper.deleteContainerByPodName(k8sPod.getName());
            }
        }
        if (!CollectionUtils.isEmpty(containerAppInstantiateInfo.getServiceList())) {
            for (K8sService k8sService : containerAppInstantiateInfo.getServiceList()) {
                containerAppInstantiateInfoMapper.deleteK8sServicePortByK8sServiceName(k8sService.getName());
            }
        }
        containerAppInstantiateInfoMapper.deleteK8sPodByAppId(applicationId);
        containerAppInstantiateInfoMapper.deleteK8sServiceByAppId(applicationId);
        containerAppInstantiateInfoMapper.deleteContainerAppInstantiateInfoByAppId(applicationId);
        return true;
    }

    public static class InstantiateContainerAppProcessor extends Thread {

        ContainerLaunchOperation actionCollection;

        OperationStatusService operationStatusService;

        OperationStatus operationStatus;

        public InstantiateContainerAppProcessor(OperationStatusService operationStatusService,
            OperationStatus operationStatus, ContainerLaunchOperation actionCollection) {
            this.operationStatusService = operationStatusService;
            this.operationStatus = operationStatus;
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            try {
                IActionIterator iterator = actionCollection.getActionIterator();
                while (iterator.hasNext()) {
                    IAction action = iterator.nextAction();
                    boolean result = action.execute();
                    if (!result) {
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("InstantiateContainerAppProcessor Exception.", e);
                operationStatus.setStatus(EnumActionStatus.FAILED);
                operationStatus.setErrorMsg("Exception happens when export image: " + e.getStackTrace().toString());
                operationStatusService.modifyOperationStatus(operationStatus);
            }
        }
    }
}
