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

package org.edgegallery.developer.model.workspace;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMepCapabilityGroup {

    private String groupId;

    private String oneLevelName;

    private String oneLevelNameEn;

    private String twoLevelName;

    private String twoLevelNameEn;

    private EnumOpenMepType type;

    private String description;

    private String descriptionEn;

    private String iconFileId;

    private String author;

    private int selectCount;

    private Date uploadTime;

    private List<OpenMepCapability> capabilityDetailList;

    public OpenMepCapabilityGroup() {
    }

    /**
     * OpenMepCapabilityGroup.
     */
    public OpenMepCapabilityGroup(String id, String oneLevelName, String oneLevelNameEn, String twoLevelName,
        String twoLevelNameEn, EnumOpenMepType type, String description, String descriptionEn) {
        this.groupId = id;
        this.oneLevelName = oneLevelName;
        this.oneLevelNameEn = oneLevelNameEn;
        this.twoLevelName = twoLevelName;
        this.twoLevelNameEn = twoLevelNameEn;
        this.type = type;
        this.description = description;
        this.descriptionEn = descriptionEn;
    }

    /**
     * setOneLevelNameEn.
     */
    public void setOneLevelNameEn(String oneLevelNameEn) {
        if (oneLevelNameEn == null || oneLevelNameEn.equals("")) {
            this.oneLevelNameEn = this.oneLevelName;
        } else {
            this.oneLevelNameEn = oneLevelNameEn;
        }
    }

    /**
     * setTwoLevelNameEn.
     */
    public void setTwoLevelNameEn(String twoLevelNameEn) {
        if (twoLevelNameEn == null || twoLevelNameEn.equals("")) {
            this.twoLevelNameEn = this.twoLevelName;
        } else {
            this.twoLevelNameEn = twoLevelNameEn;
        }
    }

    /**
     * setDescriptionEn.
     */
    public void setDescriptionEn(String descriptionEn) {
        if (descriptionEn == null || descriptionEn.equals("")) {
            this.descriptionEn = this.description;
        } else {
            this.descriptionEn = descriptionEn;
        }
    }

    /**
     * getUploadTime.
     *
     * @return
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
