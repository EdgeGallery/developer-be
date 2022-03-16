/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.model.releasedpackage;

import com.google.gson.JsonObject;
import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.common.User;

@Getter
@Setter
public class ReleasedPackage {

    private String id;

    private String appStoreAppId;

    private String appStorePackageId;

    private String name;

    private String version;

    private String provider;

    private String industry;

    private String type;

    private String architecture;

    private String shortDesc;

    private Date synchronizeDate;

    private String userId;

    private String userName;

    private String testTaskId;

    private AppPackage appPackage;

    public ReleasedPackage() {
    }

    public ReleasedPackage(JsonObject dataObj, User user) {
        id = UUID.randomUUID().toString();
        appStoreAppId = dataObj.get("appId").getAsString();
        appStorePackageId = dataObj.get("packageId").getAsString();
        name = dataObj.get("name").getAsString();
        version = dataObj.get("version").getAsString();
        provider = dataObj.get("provider").getAsString();
        industry = dataObj.get("industry").getAsString();
        type = dataObj.get("type").getAsString();
        architecture = dataObj.get("affinity").getAsString();
        shortDesc = dataObj.get("shortDesc").getAsString();
        synchronizeDate = new Date();
        userId = user.getUserId();
        userName = user.getUserName();
        testTaskId = dataObj.get("testTaskId").getAsString();
    }

    /**
     * get synchronizeDate.
     */
    public Date getSynchronizeDate() {
        return this.synchronizeDate != null ? new Date(this.synchronizeDate.getTime()) : null;
    }

    /**
     * set synchronizeDate.
     */
    public void setSynchronizeDate(Date synchronizeDate) {
        if (synchronizeDate != null) {
            this.synchronizeDate = (Date) synchronizeDate.clone();
        } else {
            this.synchronizeDate = null;
        }
    }

}
