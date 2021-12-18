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

package org.edgegallery.developer.model.apppackage.constant;

public class InputConstant {

    private InputConstant() {
        throw new IllegalStateException("InputConstant class");
    }

    public static final String TYPE_STRING = "string";

    public static final String TYPE_PASSWORD = "password";

    public static final String TYPE_TEXT = "text";

    public static final int DEFALUT_NETWORK_VLANID = 2000;

    public static final String DEFALUT_PHYSNET = "physnet2";

    public static final String INPUT_NAME_AZ = "az_dc";

    public static final String INPUT_NAME_MAP_CERTIFICATE = "mep_certificate";

    public static final String INPUT_NAME_UE_IP_SEGMENT = "ue_ip_segment";

    public static final String INPUT_NAME_MEP_IP = "mep_ip";

    public static final String INPUT_NAME_MEP_PORT = "mep_port";

    public static final String INPUT_NAME_AK = "ak";

    public static final String INPUT_NAME_SK = "sk";

    public static final String USER_DATA_PARAM_CERTIFICATE_INFO = "certificate_info";

    public static final String VDU_NAME_PREFIX = "VDU";

    public static final String INPUT_NETWORK_PREFIX = "APP_Plane0";

    public static final String INPUT_NETWORK_POSTFIX = "_Network";

    public static final String INPUT_PHYSNET_POSTFIX = "_Physnet";

    public static final String INPUT_VLANID_POSTFIX = "_VlanId";

    public static final String INPUT_PORT_IP_POSTFIX = "_IP";

    public static final String INPUT_PORT_MASK_POSTFIX = "_MASK";

    public static final String INPUT_PORT_GW_POSTFIX = "_GW";

    public static final String INPUT_PORT_MASK_DEFAULT = "255.255.255.0";

    public static final String GET_INPUT_PREFIX = "{get_input: ";

    public static final String GET_INPUT_POSTFIX = "}";

    public static final String INPUT_MEM_POSTFIX = "_MEM";

    public static final String INPUT_MEM_DES_POSTFIX = "_Mem_Size";

    public static final String INPUT_VCPU_POSTFIX = "_vCPU";

    public static final String INPUT_VCPU_DES_POSTFIX = "_vCPU_Number";

    public static final String INPUT_DATADISK_POSTFIX = "_DataDisk";

    public static final String INPUT_DATADISK_DES_POSTFIX = "_DataDisk_Size";

    public static final String INPUT_GPU_POSTFIX = "_GPU";

    public static final String INPUT_HOST_AGGR_POSTFIX = "_host_aggr";

    public static final String FLAVOR_EXTRA_SPECS_GPU = "pci_passthrough:alias";

    public static final String FLAVOR_EXTRA_SPECS_HOST_AGGR = "aggregate_instance_extra_spec";

    public static final String INPUT_CAMPUS_SEGMENT = "Enterprise_Campus";

}
