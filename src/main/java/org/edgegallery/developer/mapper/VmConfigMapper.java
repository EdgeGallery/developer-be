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

package org.edgegallery.developer.mapper;

import java.util.List;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmFlavor;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmSystem;

public interface VmConfigMapper {

    List<VmRegulation> getVmRegulation();

    List<VmSystem> getVmSystem();

    List<VmNetwork> getVmNetwork();

    VmNetwork getVmNetworkByType(String networkType);

    int saveVmCreateConfig(VmCreateConfig vmCreateConfig);

    int updateVmCreateConfig(VmCreateConfig testConfig);

    VmCreateConfig getVmCreateConfig(String projectId, String vmId);

    List<VmCreateConfig> getVmCreateConfigs(String projectId);

    List<VmCreateConfig> getVmCreateConfigStatus(String toString);

    int deleteVmCreateConfig(String projectId, String vmId);

    int deleteVmCreateConfigs(String projectId);

    VmFlavor getVmFlavor(String architecture);

    int saveVmImageConfig(VmImageConfig vmImageConfig);

    VmImageConfig getVmImage(String projectId, String vmId);

    int deleteVmImage(String projectId, String vmId);

    int updateVmImageConfig(VmImageConfig config);

    List<VmImageConfig> getVmImageConfigStatus(String toString);
}
