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

package org.edgegallery.developer.request;

import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
public class PluginRequestParam {

    private String pluginId;

    private MultipartFile pluginFile;

    private MultipartFile logoFile;

    private MultipartFile apiFile;

    private String pluginName;

    private String codeLanguage;

    private int pluginType;

    private String version;

    private String introduction;

    private String userId;

    private String userName;

    /**
     * notStrsEmpty.
     */
    public static String notStrsEmpty(String[] str) {
        if (str == null) {
            return "";
        }
        if (str.length == 0) {
            return "miss parameter " + Arrays.toString(str);
        }
        return "success";
    }
}
