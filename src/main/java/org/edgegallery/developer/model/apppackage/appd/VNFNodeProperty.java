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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VNFNodeProperty {

    @JsonProperty("vnfd_id")
    private String vnfdId;

    @JsonProperty("vnfd_version")
    private String vnfdVersion = "v1.2";

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("software_version")
    private String softwareVersion;

    @JsonProperty("product_info_name")
    private String productInfoName = "EG_MEC_APP";

    @JsonProperty("product_info_description")
    private String productInfoDescription = "EdgeGallery MEC APP";

    @JsonProperty("flavour_id")
    private String flavourId = "default";

    @JsonProperty("flavour_description")
    private String flavourDescription = "default flavor";

    @JsonProperty("ve_vnfm_vnf_enable")
    private boolean veVnfmVnfEnable = false;

    @JsonProperty("ve_vnfm_em_enable")
    private boolean veVnfmEmEnable = false;
}
