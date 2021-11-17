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

package org.edgegallery.developer.model.apppackage.appd.vducp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VDUCPProperty {

    @JsonProperty("description")
    private String description = "vducp description";

    @JsonProperty("vnic_name")
    private String vnicName = "eth0";

    @JsonProperty("order")
    private int order = 0;

    @JsonProperty("vnic_type")
    private String vnicType = "normal";

    @JsonProperty("port_security_enabled")
    private boolean portSecurityEnabled = true;
}
