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

package org.edgegallery.developer.util.samplecode.jsondata;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.util.samplecode.parameter.JsonApiParameterBean;
import org.edgegallery.developer.util.samplecode.parameter.JsonApiSchemaBean;

@Setter
@Getter
public class JsonApiMethodBean {

    private String apiType;

    private String summary;

    private String description;

    private String operationId;

    private List<String> produces;

    private List<JsonApiParameterBean> parameters;

    /**
     * get string of produces.
     *
     * @return string
     */
    public String getProduces() {
        if (produces != null) {
            return StringUtils.join(produces, ";");
        } else {
            // default is application/json
            return "application/json";
        }
    }

    /**
     * get string of header.
     *
     * @return string
     */
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (parameters == null) {
            return headers;
        }
        for (JsonApiParameterBean parameter : parameters) {
            if ("header".equals(parameter.getIn())) {
                headers.put(parameter.getName(), new Gson().toJson(parameter.getSampleData()));
            }
        }
        return headers;
    }

    /**
     * get json body.
     *
     * @return json
     */
    public String getBodyParams() {
        if (parameters == null) {
            return null;
        }
        for (JsonApiParameterBean parameter : parameters) {
            if ("body".equals(parameter.getIn())) {
                return new Gson().toJson(parameter.getSampleData());
            }
        }
        return null;
    }

    /**
     * get paths.
     *
     * @return map
     */
    public Map<String, String> getPaths() {
        Map<String, String> paths = new HashMap<>();
        if (parameters == null) {
            return null;
        }
        for (JsonApiParameterBean parameter : parameters) {
            if ("path".equals(parameter.getIn())) {
                paths.put(parameter.getName(), new Gson().toJson(parameter.getSampleData()));
            }
        }
        return paths;
    }

    @Getter
    @Setter
    static class ResponseBean {
        private String description;

        private JsonApiSchemaBean schema;
    }
}
