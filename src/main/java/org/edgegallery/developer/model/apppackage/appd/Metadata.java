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

import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class Metadata {

    @Valid
    @NotBlank
    @JsonProperty(value = "template_name")
    private String templateName;

    @Valid
    @NotBlank
    @JsonProperty(value = "template_author")
    private String templateAuthor;

    @Valid
    @NotBlank
    @JsonProperty(value = "template_version")
    private String templateVersion;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfm_type")
    private String vnfmType;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_id")
    private String vnfdId;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_version")
    private String vnfdVersion;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_name")
    private String vnfdName;

    @Valid
    @NotBlank
    @JsonProperty(value = "vnfd_description")
    private String vnfdDescription;

}
