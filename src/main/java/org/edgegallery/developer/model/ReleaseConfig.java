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
import org.edgegallery.developer.model.atp.AtpResultInfo;

@Setter
@Getter
public class ReleaseConfig {
    private String releaseId;

    private String projectId;

    private String guideFileId;

    private String appInstanceId;

    private CapabilitiesDetail capabilitiesDetail;

    private AtpResultInfo atpTest;

    private String testStatus;

    private Date createTime;

    /**
     * getCreateTime.
     */
    public Date getCreateTime() {
        if (this.createTime != null) {
            return new Date(this.createTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setCreateTime.
     */
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = (Date) createTime.clone();
        } else {
            this.createTime = null;
        }
    }
}
