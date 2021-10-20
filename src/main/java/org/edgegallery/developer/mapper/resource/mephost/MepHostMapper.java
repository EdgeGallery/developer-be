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

package org.edgegallery.developer.mapper.resource.mephost;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.resource.mephost.MepHost;

@Mapper
public interface MepHostMapper {

    int createHost(MepHost mepHost);

    int updateHostSelected(MepHost host);

    int deleteHost(@Param("hostId") String hostId);

    MepHost getHost(@Param("hostId") String hostId);

    List<MepHost> getHostsByCondition(@Param("name") String name, @Param("vimType") String vimType,
        @Param("architecture") String architecture);

    MepHost getHostsByMecHostIp(@Param("mecHost") String mecHost);
}

