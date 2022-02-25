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

package org.edgegallery.developer.service.application.vm;

import java.util.List;
import org.edgegallery.developer.model.application.vm.Network;

public interface VMAppNetworkService {

    /**
     * create vm network.
     *
     * @param applicationId vm application id
     * @param network network
     * @return
     */
    Network createNetwork(String applicationId, Network network);

    /**
     * get all vm network.
     *
     * @param applicationId vm application id
     * @return
     */
    List<Network> getAllNetwork(String applicationId);

    /**
     * get vm network.
     *
     * @param applicationId vm application id
     * @param networkId network id
     * @return
     */
    Network getNetwork(String applicationId, String networkId);

    /**
     * modify vm network.
     *
     * @param applicationId vm application id
     * @param networkId network id
     * @param network needed update network
     * @return
     */
    Boolean modifyNetwork(String applicationId, String networkId, Network network);

    /**
     * delete vm network with app id and network id.
     *
     * @param applicationId vm application id
     * @param networkId network id
     * @return
     */
    Boolean deleteNetwork(String applicationId, String networkId);

    /**
     * delete vm network with application id
     *
     * @param applicationId applicationId
     * @return
     */
    boolean deleteNetworkByAppId(String applicationId);
}
