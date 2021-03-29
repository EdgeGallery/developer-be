/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.util.samplecode.SampleData;
import org.edgegallery.developer.util.samplecode.parameter.IJsonApiParameter;
import org.edgegallery.developer.util.samplecode.parameter.JsonApiDefinitionBean;
import org.edgegallery.developer.util.samplecode.parameter.JsonApiParameterBean;

@Setter
@Getter
public class JsonApiBaseBean {

    private String swagger;

    private ApiInfoBean info;

    private String basePath = "";

    private List<String> consumes;

    private List<String> produces;

    // <path, <type, method>>
    private Map<String, JsonApiPathBean> paths;

    private Map<String, JsonApiDefinitionBean> definitions;

    private Map<String, JsonApiParameterBean> parameters;

    /**
     * analysis yaml and return each api data.
     *
     * @return List
     */
    public List<SampleData> getAllApiPaths() {
        List<SampleData> sampleDataList = new ArrayList<>();
        for (Map.Entry<String, JsonApiPathBean> entry : paths.entrySet()) {
            String url = basePath + entry.getKey();
            JsonApiPathBean apiTypes = entry.getValue();
            List<JsonApiParameterBean> params = apiTypes.getParameters();
            for (JsonApiMethodBean methodIdentify : apiTypes.getAll()) {

                // set global parameters to each method.
                if (params != null) {
                    if (methodIdentify.getParameters() == null) {
                        methodIdentify.setParameters(new ArrayList<>(params));
                    } else {
                        methodIdentify.getParameters().addAll(params);
                    }
                }
                SampleData sampleData = new SampleData();
                sampleData.setUrl(url);
                sampleData.setDescribe(methodIdentify.getDescription());
                sampleData.setType(methodIdentify.getApiType());
                sampleData.setHeaders(methodIdentify.getHeaders());
                sampleData.setContentType(methodIdentify.getProduces());
                String jsonBody = methodIdentify.getBodyParams();
                if (jsonBody != null) {
                    jsonBody = jsonBody.replaceAll("\\\"", "\\\\\"");
                }
                sampleData.setParams(jsonBody);
                sampleData.setPathData(methodIdentify.getPaths());
                sampleDataList.add(sampleData);
            }
        }
        return sampleDataList;
    }

    /**
     * replace all of ref to real object.
     */
    public void replaceAllRefs() {
        if (parameters != null) {
            for (JsonApiParameterBean bean : parameters.values()) {
                replaceSchemaObject(bean);
            }
        }

        if (definitions != null) {
            for (JsonApiDefinitionBean definition : definitions.values()) {
                if (definition.getProperties() != null) {
                    replacePropertyRefs(definition);
                }
            }
        }

        if (paths != null) {
            for (JsonApiPathBean path : paths.values()) {
                path.setParameters(replaceParameters(path.getParameters()));
                for (JsonApiMethodBean methodBean : path.getAll()) {
                    methodBean.setParameters(replaceParameters(methodBean.getParameters()));
                }
            }
        }
    }

    private List<JsonApiParameterBean> replaceParameters(List<JsonApiParameterBean> parameters) {
        if (parameters == null) {
            return new ArrayList<>();
        }
        List<JsonApiParameterBean> replaced = new ArrayList<>();
        for (JsonApiParameterBean bean : parameters) {
            JsonApiParameterBean realObj = getRealParam(bean);
            replaced.add(realObj);
        }
        return replaced;
    }

    private JsonApiParameterBean getRealParam(JsonApiParameterBean bean) {
        JsonApiParameterBean realBean = getRealParam2(bean);
        replaceSchemaObject(realBean);
        return realBean;
    }

    private void replaceSchemaObject(JsonApiParameterBean bean) {
        if (bean.getSchema() != null) {
            bean.getSchema().setRealParam(getRealObj(bean.getSchema().getRef()));
        }
    }

    private JsonApiParameterBean getRealParam2(JsonApiParameterBean bean) {
        IJsonApiParameter realObj = getRealObj(bean.getRef());
        if (realObj != null) {
            return (JsonApiParameterBean) realObj;
        } else {
            return bean;
        }
    }

    private void replacePropertyRefs(JsonApiDefinitionBean baen) {
        Collection<JsonApiDefinitionBean> values = baen.getProperties().values();
        for (JsonApiDefinitionBean property : values) {
            resetPropertyRef(property);
        }
    }

    private void resetPropertyRef(JsonApiDefinitionBean bean) {
        if (bean.getRef() != null) {
            bean.setRealObj(getRealObj(bean.getRef()));
        }
    }

    private IJsonApiParameter getRealObj(String ref) {
        if (ref == null) {
            return null;
        }
        String[] refPaths = ref.split("/");
        if (refPaths.length < 3) {
            return null;
        }
        switch (refPaths[1]) {
            case "definitions":
                return definitions != null ? definitions.get(refPaths[2]) : null;
            case "parameters":
                return parameters != null ? parameters.get(refPaths[2]) : null;
            default:
                return null;
        }
    }

    @Setter
    @Getter
    static class ApiInfoBean {
        private String version;

        private String title;

        @SerializedName(value = "x-java-interface")
        private String javaInterface;
    }
}
