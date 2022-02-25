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

package org.edgegallery.developer.mapper.application.vm;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.application.vm.VMCertificate;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;

@Mapper
public interface VMMapper {

    int createVM(@Param("applicationId") String applicationId, @Param("vm") VirtualMachine vm);

    int modifyVM(VirtualMachine vm);

    int updateVmStatus(@Param("vmId") String vmId, @Param("status") String status,
        @Param("targetImageId") Integer targetImageId);

    int deleteVM(String id);

    List<VirtualMachine> getAllVMsByAppId(String applicationId);

    VirtualMachine getVMById(@Param("applicationId") String applicationId, @Param("vmId") String vmId);

    int createVMPort(@Param("vmId") String vmId, @Param("port") VMPort port);

    int deleteAllVMPortsByVMId(String vmId);

    List<VMPort> getAllVMPortsByVMId(String vmId);

    int createVMCertificate(@Param("vmId") String vmId, @Param("certificate") VMCertificate certificate);

    int modifyVMCertificate(@Param("vmId") String vmId, @Param("certificate") VMCertificate certificate);

    VMCertificate getVMCertificate(String vmId);

}
