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

package org.edgegallery.developer.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepCreateHost;

public interface HostMapper {

    int createHost(MepCreateHost mepCreateHost);

    int updateHostSelected(MepCreateHost host);

    int deleteHost(String hostId);

    MepHost getHost(String hostId);

    List<MepHost> getHostsByUserId(String userId);

    List<MepHost> getHostsByStatus(EnumHostStatus status, String architecture, String os);

    List<MepHost> getHostsByCondition(@Param("userId") String userId, @Param("name") String name,
        @Param("ip") String ip);

    List<MepHost> selectHostsByCondition(@Param("os") String os, @Param("architecture") String architecture);

    MepHost getHostsByMecHost(String mecHost);
}

