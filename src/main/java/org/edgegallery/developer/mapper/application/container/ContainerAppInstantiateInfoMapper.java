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

package org.edgegallery.developer.mapper.application.container;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.instantiate.container.Container;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.instantiate.container.K8sPod;
import org.edgegallery.developer.model.instantiate.container.K8sService;
import org.edgegallery.developer.model.instantiate.container.K8sServicePort;

@Mapper
public interface ContainerAppInstantiateInfoMapper {

    int createContainerAppInstantiateInfo(@Param("applicationId") String applicationId,
        @Param("containerAppInstantiateInfo") ContainerAppInstantiateInfo containerAppInstantiateInfo);

    int modifyContainerAppInstantiateInfo(@Param("applicationId") String applicationId,
        @Param("containerAppInstantiateInfo") ContainerAppInstantiateInfo containerAppInstantiateInfo);

    int deleteContainerAppInstantiateInfoByAppId(String applicationId);

    ContainerAppInstantiateInfo getContainerAppInstantiateInfoAppId(String applicationId);

    int createK8sPod(@Param("applicationId") String applicationId, @Param("k8sPod") K8sPod k8sPod);

    int deleteK8sPodByAppId(String applicationId);

    List<K8sPod> getK8sPodsByAppId(String applicationId);

    int createContainer(@Param("podName") String podName, @Param("container") Container container);

    int deleteContainerByPodName(String podName);

    List<Container> getContainersByPodName(String podName);

    int createK8sService(@Param("applicationId") String applicationId, @Param("k8sService") K8sService k8sService);

    int deleteK8sServiceByAppId(String applicationId);

    List<K8sService> getK8sServiceByAppId(String applicationId);

    int createK8sServicePort(@Param("k8sServiceName") String k8sServiceName,
        @Param("k8sServicePort") K8sServicePort k8sServicePort);

    int deleteK8sServicePortByK8sServiceName(String k8sServiceName);

    List<K8sServicePort> getK8sServicePortsByK8sServiceName(String k8sServiceName);
}
