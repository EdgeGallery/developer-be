/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.model.apppackage.appd.vdu;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VDUCapability {

    Map<String, Object> virtual_compute;

    public VDUCapability(int memSize, int cpuNum, String cpuArc, int diskSize) {
        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("virtual_mem_size", memSize);

        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("num_virtual_cpu", cpuNum);
        cpu.put("cpu_architecture", cpuArc);

        Map<String, Object> disk = new LinkedHashMap<>();
        disk.put("size_of_storage", diskSize);

        Map<String, Map<String,Object>> properties = new LinkedHashMap<>();
        properties.put("virtual_memory", memory);
        properties.put("virtual_cpu", cpu);
        properties.put("virtual_local_storage", disk);

        virtual_compute.put("properties", properties);
    }

}
