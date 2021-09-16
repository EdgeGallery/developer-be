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
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.response.FormatRespDto;
import com.spencerwi.either.Either;

public interface VMAppVmService {

    VirtualMachine createVm(String applicationId, VirtualMachine virtualMachine);

    List<VirtualMachine> getAllVm(String applicationId);

    VirtualMachine getVm(String applicationId, String vmId);

    Boolean modifyVm(String applicationId, String vmId, VirtualMachine virtualMachine);

    Boolean deleteVm(String applicationId, String vmId);
}
