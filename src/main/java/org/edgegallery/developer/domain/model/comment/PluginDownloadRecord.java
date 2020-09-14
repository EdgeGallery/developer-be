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

package org.edgegallery.developer.domain.model.comment;

import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.domain.model.user.User;

@Getter
@Setter
public class PluginDownloadRecord {

    private String recordId;

    private String pluginId;

    private String downloadUserId;

    private String downloadUserName;

    /**
     * The value range is 1-5 points, and cannot be less than 1 point.
     */
    private float score;

    /**
     * Default rating: 0 / manual rating: 1, default rating is 5.
     */
    private int scoreType;

    private Date downloadTime;

    /**
     * getDownloadTime.
     */
    public Date getDownloadTime() {
        if (this.downloadTime != null) {
            return new Date(this.downloadTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setDownloadTime.
     */
    public void setDownloadTime(Date downloadTime) {
        if (downloadTime != null) {
            this.downloadTime = (Date) downloadTime.clone();
        } else {
            this.downloadTime = null;
        }
    }

    public String generateId() {
        String random = UUID.randomUUID().toString();
        return random.replace("-", "");
    }

    /**
     * Constructor of PluginDownloadRecord.
     */
    public PluginDownloadRecord(String pluginId, User user, float score, int scoreType) {
        this.recordId = generateId();
        this.pluginId = pluginId;
        this.downloadUserId = user.getUserId();
        this.downloadUserName = user.getUserName();
        this.score = score;
        this.scoreType = scoreType;
        this.downloadTime = new Date();
    }
}
