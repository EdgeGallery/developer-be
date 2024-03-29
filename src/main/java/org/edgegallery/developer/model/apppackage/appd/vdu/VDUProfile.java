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

package org.edgegallery.developer.model.apppackage.appd.vdu;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VDUProfile {

    @JsonProperty("min_number_of_instances")
    private int minNumberOfInstances = 1;

    @JsonProperty("max_number_of_instances")
    private int maxNumberOfInstances = 1;

    @JsonProperty("initial_number_of_instances")
    private int initialNumberOfInstances = 1;

    @JsonProperty("flavor_extra_specs")
    private LinkedHashMap<String, String> flavorExtraSpecs;

    public VDUProfile() {

    }

    public VDUProfile(int minNumberOfInstances, int maxNumberOfInstances, int initialNumberOfInstances) {
        this.minNumberOfInstances = minNumberOfInstances;
        this.maxNumberOfInstances = maxNumberOfInstances;
        this.initialNumberOfInstances = initialNumberOfInstances;
    }
}
