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

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VDUProperty {
    private String name = "VDU_Name";

    private String description = "VDU_Description";

    private String nfvi_constraints = "nova";

    private VDUProfile vdu_profile = new VDUProfile();

    private SwImageData sw_image_data = new SwImageData();

    private BootData bootdata  = new BootData();

    public void setNfviConstraintsAsInput(String inputName){
        this.nfvi_constraints = "{get_Input: " + inputName + "}";
    }
}
