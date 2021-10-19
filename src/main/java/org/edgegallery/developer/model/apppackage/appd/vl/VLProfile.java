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

package org.edgegallery.developer.model.apppackage.appd.vl;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VLProfile {

    private String network_name = "{get_input: network_name}";

    private String network_type = "vlan";

    private String physical_network = "{get_input: physical_network_input}";

    private String provider_segmentation_id = "{get_input: provider_segmentation_id}";

    public void setNetworkNameAsInput(String inputName){
        this.network_name = "{get_Input: " + inputName + "}";
    }

    public void setPhysicalNameAsInput(String inputName){
        this.physical_network = "{get_Input: " + inputName + "}";
    }

    public void setProviderSegmentationNameAsInput(String inputName){
        this.provider_segmentation_id = "{get_Input: " + inputName + "}";
    }
}
