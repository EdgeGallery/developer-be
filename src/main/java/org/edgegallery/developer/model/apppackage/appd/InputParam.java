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

package org.edgegallery.developer.model.apppackage.appd;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InputParam {

    @Valid
    @NotBlank
    @JsonProperty("type")
    private String type;

    @Valid
    @JsonProperty("default")
    private Object defaultValue;

    @Valid
    @JsonProperty("description")
    private String description;

    public InputParam() {

    }

    public InputParam(String type, Object defaultValue, String description) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public InputParam(Map<String, String> map) {
        this.type = map.get("type");
        this.defaultValue = map.get("default");
        this.description = map.get("description");
    }
}
