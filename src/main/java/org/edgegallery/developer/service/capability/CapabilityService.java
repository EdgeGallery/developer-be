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

package org.edgegallery.developer.service.capability;

import com.spencerwi.either.Either;
import java.util.List;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.response.FormatRespDto;

public interface CapabilityService {
    Either<FormatRespDto, Capability> create(Capability capability);

    Either<FormatRespDto, Capability> deleteById(String id);

    Either<FormatRespDto, Capability> updateById(Capability capability);

    List<Capability> findAll();

    List<Capability> findByType(String type);

    Capability findById(String id);

    List<Capability> findByGroupId(String groupId);

    List<Capability> findByProjectId(String projectId);

    Capability findByName(String name);

    List<Capability> findByNameWithFuzzy(String name);

    List<Capability> findByNameEnWithFuzzy(String nameEn);

    List<Capability> findByNameOrNameEn(String name, String nameEn);

    boolean updateSelectCountByIds(List<String> ids);
}
