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

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Getter
@Setter
@JsonPropertyOrder(alphabetic = true)
public class APPDDefinition {

    @Valid
    @NotBlank
    @JsonProperty(value = "tosca_definitions_version")
    private String toscaDefinitionsVersion;

    private String description;

    @Valid
    @NotEmpty
    private List<String> imports;

    @Valid
    @NotNull
    private Metadata metadata;

    @Valid
    @NotBlank
    @JsonProperty(value = "topology_template")
    private TopologyTemplate topologyTemplate;

}
