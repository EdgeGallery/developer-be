/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.util.samplecode.parameter;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
public class JsonApiDefinitionBean implements IJsonApiParameter, Serializable {
    private static final long serialVersionUID = -5695768504131441746L;

    private String type;

    private String describe;

    private String format;

    @SerializedName("enum")
    private List<String> enumRange;

    private transient Object example;

    @SerializedName("$ref")
    private String ref;

    private transient IJsonApiParameter realObj;

    private List<String> required;

    private Map<String, JsonApiDefinitionBean> properties;

    /**
     * get example data of each param.
     *
     * @return example data
     */
    public Object getSampleData() {
        if (example != null) {
            return example;
        }
        if (properties != null) {
            Map<String, Object> sampleData = new LinkedHashMap<>();
            for (Map.Entry<String, JsonApiDefinitionBean> entry : properties.entrySet()) {
                JsonApiDefinitionBean property = entry.getValue();
                sampleData.put(entry.getKey(), property.getSampleData());
            }
            return sampleData;
        }
        if (realObj != null) {
            return realObj.getSampleData();
        }
        switch (type) {
            case "string":
                return enumRange != null ? StringUtils.join(enumRange, ";") : "example_String";
            case "integer":
                return 100;
            case "number":
                return 12.45;
            default:
                return "";
        }
    }
}


