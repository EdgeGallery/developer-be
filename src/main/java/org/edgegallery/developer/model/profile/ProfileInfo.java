/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(value = {"filePath", "deployFilePath", "configFilePath"})
public class ProfileInfo {
    /**
     * profile id.
     */
    private String id;

    /**
     * profile name.
     */
    private String name;

    /**
     * profile chinese description.
     */
    private String description;

    /**
     * profile english description.
     */
    private String descriptionEn;

    /**
     * profile zip file path.
     */
    private String filePath;

    /**
     * app deploy file path list.
     */
    private Map<String, String> deployFilePath;

    /**
     * config file path.
     */
    private String configFilePath;

    /**
     * app deploy sequence.
     */
    private List<String> seq;

    /**
     * profile create time.
     */
    private Date createTime;

    /**
     * profile type.
     */
    private String type;

    /**
     * profile industry.
     */
    private String industry;

    /**
     * topo file path.
     */
    private String topoFilePath;

    /**
     * get profile create time.
     */
    public Date getCreateTime() {
        if (this.createTime != null) {
            return new Date(this.createTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * set profile create time.
     */
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = (Date) createTime.clone();
        } else {
            this.createTime = null;
        }
    }
}
