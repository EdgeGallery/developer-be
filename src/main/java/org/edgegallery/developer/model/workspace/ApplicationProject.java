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

package org.edgegallery.developer.model.workspace;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationProject {

    // normal data start
    private String id;

    private EnumProjectType projectType;

    private String name;

    private String version;

    private String provider;

    private List<String> platform;

    // add to match app store
    private String type;

    private List<String> industry;

    private String description;

    private String iconFileId;

    // Online or Deploying or Deployed or Testing or Tested
    private EnumProjectStatus status;

    private List<OpenMepCapabilityGroup> capabilityList;

    private String lastTestId;

    private String userId;

    private Date createDate;

    private String openCapabilityId;

    /**
     * getId.
     */
    public String getId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        return this.id;
    }

    public void setIconFileId(String iconFileId) {
        this.iconFileId = iconFileId;
    }

    /**
     * getCreateDate.
     */
    public Date getCreateDate() {
        if (this.createDate != null) {
            return new Date(this.createDate.getTime());
        } else {
            return null;
        }
    }

    /**
     * setCreateDate.
     */
    public void setCreateDate(Date createDate) {
        if (createDate != null) {
            this.createDate = (Date) createDate.clone();
        } else {
            this.createDate = null;
        }
    }

}
