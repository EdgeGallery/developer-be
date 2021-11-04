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

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VDUCPAttributes {

    private String ipv4_address;

    private String ipv6_address = "00::00";

    private String mac = "00::00::00::00::00::00";

    private String ipv4_vip_address = "0.0.0.0";

    private String ipv6_vip_address = "00:00";
}
