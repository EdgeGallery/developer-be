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
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;

@Mapper
public interface VMInstantiateInfoMapper {

    int createVMInstantiateInfo(@Param("vmId")String vmId, @Param("vmInstantiateInfo")VMInstantiateInfo vmInstantiateInfo);

    int modifyVMInstantiateInfo(@Param("vmId")String vmId, @Param("vmInstantiateInfo")VMInstantiateInfo vmInstantiateInfo);

    int deleteVMInstantiateInfo(String vmId);

    VMInstantiateInfo getVMInstantiateInfo(String vmId);

    int createPortInstantiateInfo(@Param("vmId")String vmId, @Param("portInstantiateInfo")PortInstantiateInfo portInstantiateInfo);

    int deletePortInstantiateInfo(String vmId);

    List<PortInstantiateInfo> getPortInstantiateInfo(String vmId);
}
