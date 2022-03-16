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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppdConstants {

    private AppdConstants() {
        throw new IllegalStateException("AppdConstants class");
    }

    public static final String VNF_NODE_NAME = "Simple_VNF";

    public static final String APP_CONFIGURATION_NODE_NAME = "app_configuration";

    public static final String PORT_VNIC_NAME_PREFIX = "eth";

    public static final String NETWORK_NAME_PREFIX = "MEC_";

    public static final String DEFAULT_NETWORK_INTERNET = "MEC_APP_Public";

    public static final String DEFAULT_NETWORK_N6 = "MEC_APP_Private";

    private static final String DEFAULT_NETWORK_MP1 = "MEC_APP_MP1";

    private static final List<String> NETWORK_NAME_SORTED_LIST = Arrays
        .asList(DEFAULT_NETWORK_MP1, "MEC_APP_Internet", DEFAULT_NETWORK_INTERNET, "MEC_APP_N6", DEFAULT_NETWORK_N6,
            "Internal_Network");

    public static final String GROUPS_NODE_NAME = "AntiAffinityGroup";

    public static final String POLICY_NODE_NAME = "antiaffinity_policy";

    public static final int MEMORY_SIZE_UNIT = 1024;

    public static List<String> getNetworkNameSortedList() {
        return Collections.unmodifiableList(NETWORK_NAME_SORTED_LIST);
    }
}
