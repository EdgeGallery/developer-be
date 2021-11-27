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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.edgegallery.developer.model.instantiate.container.PodContainers;
import org.edgegallery.developer.model.instantiate.container.PodEvents;
import org.edgegallery.developer.model.instantiate.container.PodEventsRes;
import org.edgegallery.developer.model.instantiate.container.PodStatusInfo;
import org.edgegallery.developer.model.instantiate.container.PodStatusInfos;
import org.edgegallery.developer.model.instantiate.container.ServiceInfo;
import org.edgegallery.developer.model.instantiate.container.ServicePort;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.container.Container;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.instantiate.container.K8sPod;
import org.edgegallery.developer.model.instantiate.container.K8sService;
import org.edgegallery.developer.model.instantiate.container.K8sServicePort;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.service.application.action.impl.InstantiateAppAction;
import org.edgegallery.developer.service.application.common.EnumInstantiateStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InstantiateContainerAppAction extends InstantiateAppAction {

    ContainerAppOperationServiceImpl containerAppOperationService = (ContainerAppOperationServiceImpl) SpringContextUtil
        .getBean(ContainerAppOperationServiceImpl.class);

    private static Gson gson = new Gson();

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId, EnumAppInstantiateStatus status) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        instantiateInfo.setStatus(status);
        return containerAppOperationService.updateInstantiateInfo(applicationId, instantiateInfo);
    }

    public boolean saveWorkloadToInstantiateInfo(PodStatusInfos status, PodEventsRes events) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        if (!CollectionUtils.isEmpty(status.getPods())) {
            List<PodStatusInfo> statusInfoLst = status.getPods();
            for (PodStatusInfo podStatusInfo : statusInfoLst) {
                K8sPod pod = getPodByName(instantiateInfo, podStatusInfo.getPodname());
                pod.setPodStatus(podStatusInfo.getPodstatus());
                for (PodContainers containerTmp : podStatusInfo.getContainers()) {
                    Container container = new Container();
                    container.setName(containerTmp.getContainername());
                    if (null != containerTmp.getMetricsusage()) {
                        container.setCpuUsage(containerTmp.getMetricsusage().getCpuusage());
                        container.setMemUsage(containerTmp.getMetricsusage().getMemusage());
                        container.setDiskUsage(containerTmp.getMetricsusage().getDiskusage());
                    }
                    pod.getContainerList().add(container);
                }
            }
            List<ServiceInfo> serviceInfoLst = status.getServices();
            for (ServiceInfo service : serviceInfoLst) {
                K8sService k8sService = getServiceByName(instantiateInfo, service.getServiceName());
                k8sService.setType(service.getType());
                for (ServicePort port : service.getPorts()) {
                    K8sServicePort k8sServicePort = new K8sServicePort();
                    k8sServicePort.setPort(port.getPort());
                    k8sServicePort.setNodePort(port.getNodePort());
                    k8sServicePort.setTargetPort(port.getTargetPort());
                    k8sService.getServicePortList().add(k8sServicePort);
                }
            }
        }
        if (!CollectionUtils.isEmpty(events.getPods())) {
            List<PodEvents> eventsInfoLst = events.getPods();
            for (PodEvents podEventInfo : eventsInfoLst) {
                K8sPod pod = getPodByName(instantiateInfo, podEventInfo.getPodName());
                pod.setEventsInfo(Arrays.toString(podEventInfo.getPodEventsInfo()));
            }
        }
        return containerAppOperationService.updateInstantiateInfo(applicationId, instantiateInfo);
    }

    private K8sService getServiceByName(ContainerAppInstantiateInfo instantiateInfo, String serviceName) {
        for (K8sService service : instantiateInfo.getServiceList()) {
            if (serviceName.equals(service.getName())) {
                return service;
            }
        }
        K8sService k8sService = new K8sService();
        k8sService.setName(serviceName);
        instantiateInfo.getServiceList().add(k8sService);
        return k8sService;
    }

    private K8sPod getPodByName(ContainerAppInstantiateInfo instantiateInfo, String podName) {
        for (K8sPod pod : instantiateInfo.getPods()) {
            if (podName.equals(pod.getName())) {
                return pod;
            }
        }
        K8sPod pod = new K8sPod();
        pod.setName(podName);
        instantiateInfo.getPods().add(pod);
        return pod;
    }

    public Map<String, String> getInputParams(MepHost mepHost) {
        return null;
    }

    public EnumInstantiateStatus queryInstantiateStatus(String appInstanceId, MepHost mepHost) {
        int waitingTime = 0;
        while (waitingTime < TIMEOUT) {
            String workStatus = HttpClientUtil.getWorkloadStatus(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
                mepHost.getLcmPort(), appInstanceId, getContext().getUserId(), getContext().getToken());
            LOGGER.info("Container app instantiate workStatus: {}", workStatus);
            String workEvents = HttpClientUtil.getWorkloadEvents(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
                mepHost.getLcmPort(), appInstanceId, getContext().getUserId(), getContext().getToken());
            LOGGER.info("Container app instantiate workEvents: {}", workEvents);
            if (null != workStatus && null != workEvents) {
                Type type = new TypeToken<PodStatusInfos>() {
                }.getType();
                PodStatusInfos status = gson.fromJson(workStatus, type);
                Type typeEvents = new TypeToken<PodEventsRes>() {
                }.getType();
                PodEventsRes events = gson.fromJson(workEvents, typeEvents);
                if (!StringUtils.isEmpty(status.getPods().get(0).getContainers()[0].getContainername())) {
                    saveWorkloadToInstantiateInfo(status, events);
                    return EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS;
                }
            }

            try {
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.error("Distribute package sleep failed.");
                return EnumInstantiateStatus.INSTANTIATE_STATUS_ERROR;
            }
        }
        return EnumInstantiateStatus.INSTANTIATE_STATUS_TIMEOUT;

    }
}
