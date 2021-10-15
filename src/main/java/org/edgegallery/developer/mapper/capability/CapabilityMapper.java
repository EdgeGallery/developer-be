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

package org.edgegallery.developer.mapper.capability;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.capability.Capability;

@Mapper
public interface CapabilityMapper {
    int insert(Capability capability);

    int updateById(Capability capability);

    int deleteById(@Param("id") String id);

    List<Capability> selectAll();

    List<Capability> selectByType(@Param("type") String type);

    Capability selectById(@Param("id") String id);

    List<Capability> selectByGroupId(@Param("groupId") String groupId);

    List<Capability> selectByNameWithFuzzy(@Param("name") String name);

    List<Capability> selectByNameEnWithFuzzy(@Param("nameEn") String nameEn);

    Capability selectByName(@Param("name") String name);

    List<Capability> selectByNameOrNameEn(@Param("name") String name, @Param("nameEn") String nameEn);

    List<Capability> selectByProjectId(@Param("projectId") String projectId);

    int updateSelectCountByIds(List<String> ids);

    List<Capability> selectByApiFileId(@Param("apiFileId") String apiFileId);
}
