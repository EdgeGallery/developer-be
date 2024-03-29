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
import org.edgegallery.developer.model.capability.CapabilityGroupStat;

public interface CapabilityGroupStatService {

    /**
     * get all capability include group.
     *
     * @return
     */
    List<CapabilityGroupStat> findAll();

    /**
     * get all capability include group by type.
     *
     * @param type group type
     * @return
     */
    List<CapabilityGroupStat> findByType(String type);
}
