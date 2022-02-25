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
import org.edgegallery.developer.model.capability.CapabilityGroup;

public interface CapabilityGroupService {
    /**
     * create capability group.
     *
     * @param capabilityGroup capabilityGroup
     * @return
     */
    CapabilityGroup create(CapabilityGroup capabilityGroup);

    /**
     * delete capability group.
     *
     * @param id group id
     * @return
     */
    boolean deleteById(String id);

    /**
     * get all capability group.
     *
     * @return
     */
    List<CapabilityGroup> findAll();

    /**
     * get capability group by type.
     *
     * @param type group typea
     * @return
     */
    List<CapabilityGroup> findByType(String type);

    /**
     * get capability group by id.
     *
     * @param id group id
     * @return
     */
    CapabilityGroup findById(String id);

    /**
     * get capability group by name.
     *
     * @param name group name
     * @return
     */
    CapabilityGroup findByName(String name);

    /**
     * get capability group by name or english name.
     *
     * @param name group chinese name
     * @param nameEn group english name
     * @return
     */
    List<CapabilityGroup> findByNameOrNameEn(String name, String nameEn);
}
