/*
 * Copyright 2022 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.apppackage.csar.appdconverter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class VmDefaultInputData {
    private static final String VM_PACKAGE_TEMPLATE_INPUT_PATH
        = "./configs/template/appd/vm_appd_inputs_default_data.yaml";

    private static final Map<String, LinkedHashMap> defaultInputData = new LinkedHashMap<>();

    public static Object getDefaultData(String key) {
        initDefaultData();
        return defaultInputData.containsKey(key) ? defaultInputData.get(key).get("default") : null;
    }

    private static void initDefaultData() {
        try (InputStream inputStream = new FileInputStream(VM_PACKAGE_TEMPLATE_INPUT_PATH)) {
            Yaml yaml = new Yaml(new SafeConstructor());
            defaultInputData.clear();
            defaultInputData.putAll(yaml.load(inputStream));
        } catch (IOException e) {
            throw new FileOperateException("init vm inputs read file failed.", ResponseConsts.RET_LOAD_YAML_FAIL);
        }
    }
}
