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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMepCapabilityDetail {

    private String detailId;

    private String groupId;

    private String service;

    private String version;

    private String description;

    private String provider;

    // download or show api
    private String apiFileId;

    private String guideFileId;

    private Date uploadTime;

    private int port;

    private String host;

    private String protocol;

    private String appId;

    private String packageId;

    private String userId;

    public OpenMepCapabilityDetail() {
    }

    /**
     * OpenMepCapabilityDetail.
     */
    public OpenMepCapabilityDetail(String id, String groupId, String service, String version, String description) {
        this.detailId = id;
        this.groupId = groupId;
        this.service = service;
        this.version = version;
        this.description = description;
    }
}
