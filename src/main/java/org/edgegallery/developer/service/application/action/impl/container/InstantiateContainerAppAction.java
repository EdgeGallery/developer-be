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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.edgegallery.developer.model.deployyaml.PodContainers;
import org.edgegallery.developer.model.deployyaml.PodEvents;
import org.edgegallery.developer.model.deployyaml.PodEventsRes;
import org.edgegallery.developer.model.deployyaml.PodStatusInfo;
import org.edgegallery.developer.model.deployyaml.PodStatusInfos;
import org.edgegallery.developer.model.deployyaml.ServiceInfo;
import org.edgegallery.developer.model.deployyaml.ServicePort;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class InstantiateContainerAppAction extends InstantiateAppAction {

    @Autowired
    private ContainerAppOperationServiceImpl containerAppOperationService;

    private static Gson gson = new Gson();

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        return containerAppOperationService.updateInstantiateInfo(appInstanceId, instantiateInfo);
    }

    public boolean saveWorkloadToInstantiateInfo(String workStatus, String workEvents) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        Type type = new TypeToken<PodStatusInfos>() { }.getType();
        PodStatusInfos status = gson.fromJson(workStatus, type);
        Type typeEvents = new TypeToken<PodEventsRes>() { }.getType();
        PodEventsRes events = gson.fromJson(workEvents, typeEvents);
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
            //TODO make ServiceInfo/PodStatusInfo and K8sService/K8sPod same model etc.
            List<ServiceInfo> serviceInfoLst = status.getServices();
            for(ServiceInfo service: serviceInfoLst){
                K8sService k8sService = new K8sService();
                k8sService.setName(service.getServiceName());
                k8sService.setType(service.getType());
                for(ServicePort port: service.getPorts()){
                    K8sServicePort k8sServicePort = new K8sServicePort();
                    k8sServicePort.setPort(port.getPort());
                    k8sServicePort.setNodePort(port.getNodePort());
                    k8sServicePort.setTargetPort(port.getTargetPort());
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
                //merge workStatus and workEvents
                saveWorkloadToInstantiateInfo(workStatus, workEvents);
                return EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS;
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
