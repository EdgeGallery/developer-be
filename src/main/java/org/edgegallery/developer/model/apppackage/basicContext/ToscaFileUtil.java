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

package org.edgegallery.developer.model.apppackage.basicContext;


import org.edgegallery.developer.model.apppackage.IToscaContentEnum;

public class ToscaFileUtil {

    private ToscaFileUtil() {}

    public static boolean toCheckData() {
        return false;
    }

    public static String toStringBy(IToscaContentEnum type, String value) {
        return (type.getName() + type.getSplit() + value).trim();
    }
}
