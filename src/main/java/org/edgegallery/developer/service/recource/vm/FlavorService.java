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

package org.edgegallery.developer.service.recource.vm;

import java.util.List;
import org.edgegallery.developer.model.resource.vm.Flavor;

public interface FlavorService {

    /**
     * get all vm flavor.
     *
     * @return
     */
    List<Flavor> getAllFlavors();

    /**
     * get flavor by id.
     *
     * @param flavorId flavor id
     * @return
     */
    Flavor getFlavorById(String flavorId);

    /**
     * create vm flavor.
     *
     * @param flavor flavor
     * @return
     */
    Flavor createFlavor(Flavor flavor);

    /**
     * delete vm flavor by id.
     *
     * @param flavorId flavor id
     * @return
     */
    Boolean deleteFlavorById(String flavorId);
}
