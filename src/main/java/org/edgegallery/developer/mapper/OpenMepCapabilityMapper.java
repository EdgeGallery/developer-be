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
import org.edgegallery.developer.model.workspace.OpenMepApi;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;

public interface OpenMepCapabilityMapper {

    int saveGroup(OpenMepCapabilityGroup group);

    String getGroupIdByDetailId(String detailId);

    int deleteGroup(String groupId);

    int updateSelectCount(String groupId);

    int saveCapability(OpenMepCapabilityDetail capability);

    int deleteCapability(String capabilityId);

    OpenMepCapabilityGroup getGroup(String groupId);

    OpenMepCapabilityGroup getEcoGroupByName(String name);

    OpenMepCapabilityDetail getDetail(String capabilityId);

    List<OpenMepCapabilityDetail> getDetailByGroupId(String groupId);

    OpenMepCapabilityDetail getOpenMepByFileId(String fileId);

    OpenMepCapabilityDetail updateOpenMepByFileId(OpenMepCapabilityDetail capability);

    List<OpenMepCapabilityGroup> getOpenMepCapabilitiesDetail();

    List<OpenMepCapabilityGroup> getOpenMepCapabilities();

    OpenMepCapabilityGroup getOpenMepCapabilitiesByGroupId(String groupId);

    List<OpenMepCapabilityGroup> getOpenMepList(String type);

    List<OpenMepApi> getOpenMepEcoList();

    OpenMepCapabilityDetail getDetailByApiFileId(String apiFileId);

    List<OpenMepCapabilityGroup> getOpenMepListByCondition(@Param("userId") String userId,
        @Param("twoLevelName") String twoLevelName, @Param("twoLevelNameEn") String twoLevelNameEn);

}
