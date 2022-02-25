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
import org.edgegallery.developer.model.application.vm.EnumVMStatus;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.common.User;

public interface VMAppVmService {

    /**
     * create vm.
     *
     * @param applicationId vm application id
     * @param virtualMachine vm
     * @return
     */
    VirtualMachine createVm(String applicationId, VirtualMachine virtualMachine);

    /**
     * get all created vm with application id.
     *
     * @param applicationId vm application id
     * @return
     */
    List<VirtualMachine> getAllVm(String applicationId);

    /**
     * get one vm with application id and vm id.
     *
     * @param applicationId vm application id
     * @param vmId vm id
     * @return
     */
    VirtualMachine getVm(String applicationId, String vmId);

    /**
     * update one vm.
     *
     * @param applicationId vm application id
     * @param vmId vm id
     * @param virtualMachine vm
     * @return
     */
    Boolean modifyVm(String applicationId, String vmId, VirtualMachine virtualMachine);

    /**
     * delete one vm.
     *
     * @param applicationId vm application id
     * @param vmId vm id
     * @param user operator
     * @return
     */
    Boolean deleteVm(String applicationId, String vmId, User user);

    /**
     * update vm status.
     *
     * @param vmId vm id
     * @param status vm status
     * @param targetImageId vm include certain image
     * @return
     */
    boolean updateVmStatus(String vmId, EnumVMStatus status, Integer targetImageId);

    /**
     * delete vm with application id
     *
     * @param applicationId application id
     * @param user operator
     */
    void deleteVmByAppId(String applicationId, User user);
}
