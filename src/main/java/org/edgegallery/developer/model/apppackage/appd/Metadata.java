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
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Metadata {

    @Valid
    @NotBlank
    @JsonProperty("template_name")
    private String templateName = "Custom-MEC-APP";

    @Valid
    @NotBlank
    @JsonProperty("template_author")
    private String templateAuthor = "EdgeGallery";

    @Valid
    @NotBlank
    @JsonProperty("template_version")
    private String templateVersion = "1.0.0";

    @Valid
    @NotBlank
    @JsonProperty("vnfm_type")
    private String vnfmType = "MEPM";

    @Valid
    @NotBlank
    @JsonProperty("vnfd_id")
    private String vnfdId = "Custom VNFD ID";

    @Valid
    @NotBlank
    @JsonProperty("vnfd_version")
    private String vnfdVersion = "v1.2";

    @Valid
    @NotBlank
    @JsonProperty("vnfd_name")
    private String vnfdName ="Custom VNFD Name";

    @Valid
    @NotBlank
    @JsonProperty("vnfd_description")
    private String vnfdDescription ="custom VNFD Description";

}
