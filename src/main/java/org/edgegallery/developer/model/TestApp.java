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
import lombok.ToString;

@ToString
public class TestApp {

    private String appId;

    private String appName;

    private String appFile;

    private String logoFile;

    private String affinity;

    private String industry;

    private String appDesc;

    private Date uploadTime;

    private String userId;

    private String appVersion;

    private String type;

    private TestTask task;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppFile() {
        return appFile;
    }

    public void setAppFile(String appFile) {
        this.appFile = appFile;
    }

    public String getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(String logoFile) {
        this.logoFile = logoFile;
    }

    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TestTask getTask() {
        return task;
    }

    public void setTask(TestTask task) {
        this.task = task;
    }

}
