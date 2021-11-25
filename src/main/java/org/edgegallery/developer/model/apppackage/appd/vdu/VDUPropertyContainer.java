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
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VDUPropertyContainer {

    @JsonProperty("name")
    private String name = "logic0";

    @JsonProperty("description")
    private String description = "node logic template";

    @JsonProperty("vdu_profile")
    private VDUProfile vduProfile = new VDUProfile(1,2,1);

    @JsonProperty("sw_image_data")
    private SwImageData swImageData = new SwImageData();
}
