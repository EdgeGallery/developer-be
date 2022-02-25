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

import java.util.List;
import org.edgegallery.developer.model.capability.Capability;

public interface CapabilityService {
    /**
     * create capability.
     *
     * @param capability capability
     * @return
     */
    Capability create(Capability capability);

    /**
     * delete capability by id.
     *
     * @param id capability id
     * @return
     */
    boolean deleteById(String id);

    /**
     * update capability by id.
     *
     * @param capability capability
     * @return
     */
    Capability updateById(Capability capability);

    /**
     * get all capabilities.
     *
     * @return
     */
    List<Capability> findAll();

    /**
     * get capabilities by type.
     *
     * @param type capability type
     * @return
     */
    List<Capability> findByType(String type);

    /**
     * get capability by id.
     *
     * @param id capability id
     * @return
     */
    Capability findById(String id);

    /**
     * get capabilities by group id.
     *
     * @param groupId
     * @return
     */
    List<Capability> findByGroupId(String groupId);

    /**
     * get capabilities by applicationId.
     *
     * @param applicationId applicationId
     * @return
     */
    List<Capability> findByApplicationId(String applicationId);

    /**
     * get capabilities by api file id.
     *
     * @param apiFileId api file id
     * @return
     */
    List<Capability> findByApiFileId(String apiFileId);

    /**
     * get capabilities by chinese name.
     *
     * @param name chinese name
     * @return
     */
    List<Capability> findByNameWithFuzzy(String name);

    /**
     * get capabilities by english name.
     *
     * @param nameEn english name
     * @return
     */
    List<Capability> findByNameEnWithFuzzy(String nameEn);

    /**
     * get capabilities by chinese name or english name.
     *
     * @param name chinese name
     * @param nameEn english name
     * @return
     */
    List<Capability> findByNameOrNameEn(String name, String nameEn);

    /**
     * update select count in capability.
     *
     * @param ids capability id list
     * @return
     */
    boolean updateSelectCountByIds(List<String> ids);
}
