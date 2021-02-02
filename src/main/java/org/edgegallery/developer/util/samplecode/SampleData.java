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

package org.edgegallery.developer.util.samplecode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class SampleData {

    private String url;

    // get/put/post/delete
    private String type;

    private String contentType = "application/json";

    private Map<String, String> headers;

    private Map<String, String> pathData;

    // json data for body or form
    private String params;

    private String describe;

    /**
     * generate sample code.
     *
     * @param id id
     * @return string
     */
    public String toSampleCode(int id) {
        List<String> lines = new ArrayList<>();
        lines.add("    /**");
        lines.add(String.format("    * %s", describe));
        lines.add("    */");
        lines.add(String.format("    public static String apiSample%d() throws IOException {", id));
        lines.add(String.format("        String url = \"%s\";", url));
        lines.add(String.format("        String type = \"%s\";", type.toUpperCase()));
        lines.add(String.format("        String contentType = \"%s\";", contentType));
        if (params != null) {
            lines.add(String.format("        String params = \"%s\";", params));
        } else {
            lines.add("        String params = null;");
        }
        replacePathDatas(lines);
        setHeader(lines);
        lines.add("        HttpRequest getRequest = new HttpRequest();");
        lines.add("        String result = getRequest.doRequest(url, type, headerMap, contentType, params);");
        lines.add("        return result;");
        lines.add("    }");
        return StringUtils.join(lines, "\n");
    }

    private void replacePathDatas(List<String> lines) {
        if (pathData != null) {
            for (Map.Entry<String, String> entry : pathData.entrySet()) {
                lines.add(String.format("        url = url.replaceAll(\"%s\", \"%s\");",
                    String.format("\\\\{%s\\\\}", entry.getKey()), entry.getValue()));
            }
        }
    }

    private void setHeader(List<String> lines) {
        if (headers != null) {
            lines.add("        Map<String, String> headerMap = new HashMap<String, String>();");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                lines.add(String.format("        headerMap.put(\"%s\", \"%s\");", entry.getKey(), entry.getValue()));
            }
        }
    }
}
