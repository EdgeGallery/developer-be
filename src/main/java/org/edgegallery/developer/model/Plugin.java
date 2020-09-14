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

package org.edgegallery.developer.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Plugin {

    /**
     * plugin id,Use database strategy uuid_generate_v4 (), automatically generated.
     */
    private String pluginId;

    private String pluginName;

    /**
     * plugin introduction,can be null or empty.
     */
    private String introduction;

    private float satisfaction;

    /**
     * only be one of{JAVA,Python,Go,.Net,PHP}.
     */
    private String codeLanguage;

    /**
     * pluginType:1 Plugin pluginType:2 SDK.
     */
    private int pluginType;

    /**
     * can be null or empty.
     */
    private String version;

    private int downloadCount;

    private String logoFile;

    private String pluginFile;

    private Date uploadTime;

    private String userId;

    private String userName;

    private int pluginSize;

    private String apiFile;

    private int scoreCount;

    /**
     * getUploadTime.
     */
    public Date getUploadTime() {
        if (this.uploadTime != null) {
            return new Date(this.uploadTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setUploadTime.
     */
    public void setUploadTime(Date uploadTime) {
        if (uploadTime != null) {
            this.uploadTime = (Date) uploadTime.clone();
        } else {
            this.uploadTime = null;
        }
    }
}
