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
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.common.Consts;

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
    @Size(max = Consts.LENGTH_64, message = "profile name can not more than 64.")
    private String name;

    /**
     * profile chinese description.
     */
    @Size(max = Consts.LENGTH_255, message = "profile description can not more than 255.")
    private String description;

    /**
     * profile english description.
     */
    @Size(max = Consts.LENGTH_255, message = "profile description can not more than 255.")
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
    @Size(max = Consts.LENGTH_255, message = "profile app deploy seq can not more than 255.")
    private List<String> seq;

    /**
     * profile create time.
     */
    private Date createTime;

    /**
     * profile type.
     */
    @Size(max = Consts.LENGTH_64, message = "profile type can not more than 64.")
    private String type;

    /**
     * profile industry.
     */
    @Size(max = Consts.LENGTH_64, message = "profile industry can not more than 64.")
    private String industry;

    /**
     * topo file path.
     */
    private String topoFilePath;

    /**
     * get profile create time.
     */
    public Date getCreateTime() {
        return this.createTime != null ? new Date(this.createTime.getTime()) : null;
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
