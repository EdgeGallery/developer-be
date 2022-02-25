/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.proxy;

import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;

public interface ReverseProxyService {

    /**
     * add reverse proxy.
     *
     * @param hostId host id
     * @param hostConsolePort connect host port.
     * @param token token
     */
    void addReverseProxy(String hostId, int hostConsolePort, String token);

    /**
     * delete  reverse proxy.
     *
     * @param hostId host id
     * @param hostConsolePort connect host port.
     * @param token token
     */
    void deleteReverseProxy(String hostId, int hostConsolePort, String token);

    /**
     * get connect vm url.
     *
     * @param applicationId applicationId
     * @param vmId vmId
     * @param userId userId
     * @param token token
     * @return
     */
    String getVmConsoleUrl(String applicationId, String vmId, String userId, String token);

    /**
     * get connect vm info.
     *
     * @param applicationId applicationId
     * @param vmId vmId
     * @param userId userId
     * @param xsrfValue token value
     * @return
     */
    SshResponseInfo getVmSshResponseInfo(String applicationId, String vmId, String userId, String xsrfValue);

    /**
     * get container info in vm.
     *
     * @param applicationId applicationId
     * @param userId userId
     * @param xsrfValue token value
     * @return
     */
    SshResponseInfo getContainerSshResponseInfo(String applicationId, String userId, String xsrfValue);
}
