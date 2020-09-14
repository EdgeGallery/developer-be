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
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
public class JsonApiParameterBean implements IJsonApiParameter, Serializable {
    private static final long serialVersionUID = 513605081045939819L;

    private String name;

    private String in;

    private String description;

    private boolean required = false;

    private String type = "null";

    private JsonApiSchemaBean schema;

    @SerializedName(value = "$ref")
    private String ref;

    private transient IJsonApiParameter realObj;

    @SerializedName("enum")
    private List<String> enumRange;

    @SerializedName("x-example")
    private transient Object example;

    /**
     * get sample data from the json.
     */
    public Object getSampleData() {
        if (example != null) {
            return example;
        } else {
            return getExampleByType();
        }
    }

    private Object getExampleByType() {
        if (realObj != null) {
            return realObj.getSampleData();
        }
        if (schema != null) { // return the ref to find the real class
            return schema.getExampleData();
        }
        if (type == null) {
            return "";
        } else {
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
}
