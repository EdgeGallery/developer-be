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

package org.edgegallery.developer.model.resource.container;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HarborImage {

    @JsonProperty(value = "artifact_count")
    private Integer artifactCount;

    @JsonProperty(value = "creation_time")
    private String creationTime;

    private Integer id;

    private String name;

    @JsonProperty(value = "project_id")
    private Integer projectId;

    @JsonProperty(value = "pull_count")
    private Integer pullCount;

    @JsonProperty(value = "update_time")
    private String updateTime;


}