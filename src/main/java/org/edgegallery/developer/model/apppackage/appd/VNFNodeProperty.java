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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder(alphabetic = true)
public class VNFNodeProperty {

    private String vnfd_id;

    private String vnfd_version = "v1.2";

    private String provider;

    private String product_name;

    private String software_version;

    private String product_info_name = "EG_MEC_APP";

    private String product_info_description = "EdgeGallery MEC APP";

    private String flavour_id = "default";

    private String flavour_description = "default flavor";

    private boolean ve_vnfm_vnf_enable = false;

    private boolean ve_vnfm_em_enable = false;
}
