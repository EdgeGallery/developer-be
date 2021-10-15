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

package org.edgegallery.developer.util;

import java.util.HashMap;
import java.util.Map;

public class InputParameterUtil {

    private InputParameterUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * getParams.
     *
     * @param str str
     * @return
     */
    public static Map<String, String> getParams(String str) {
        String[] arr = str.split(";");
        Map<String, String> params = new HashMap<>();
        for (String temp : arr) {
            String[] keyValue = temp.trim().split("=");
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0];
            String value = keyValue[1];
            params.put(key, value);
        }
        return params;

    }


}
