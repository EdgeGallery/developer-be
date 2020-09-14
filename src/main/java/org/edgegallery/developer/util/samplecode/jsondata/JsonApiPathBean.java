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

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.util.samplecode.parameter.JsonApiParameterBean;

public class JsonApiPathBean {

    @SerializedName("get")
    private JsonApiMethodBean httpGet;

    @SerializedName("post")
    private JsonApiMethodBean httpPost;

    @SerializedName("put")
    private JsonApiMethodBean httpPut;

    @SerializedName("delete")
    private JsonApiMethodBean httpDelete;

    @Getter
    @Setter
    private List<JsonApiParameterBean> parameters;

    /**
     * get all api.
     */
    public List<JsonApiMethodBean> getAll() {
        List<JsonApiMethodBean> all = new ArrayList<>();
        addNotNull(all, httpDelete, "DELETE");
        addNotNull(all, httpGet, "GET");
        addNotNull(all, httpPost, "POST");
        addNotNull(all, httpPut, "PUT");
        return all;
    }

    private void addNotNull(final List<JsonApiMethodBean> all, final JsonApiMethodBean method, String type) {
        if (method != null) {
            method.setApiType(type);
            all.add(method);
        }
    }
}
