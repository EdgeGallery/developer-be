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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMepCapabilityDetail {

    private String detailId;

    private String groupId;

    private String service;

    private String serviceEn;

    private String version;

    private String description;

    private String provider;

    // download or show api
    private String apiFileId;

    private String guideFileId;

    private String guideFileIdEn;

    private String uploadTime;

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
    public OpenMepCapabilityDetail(String id, String groupId, String service, String serviceEn, String version,
        String description) {
        this.detailId = id;
        this.groupId = groupId;
        this.service = service;
        this.serviceEn = serviceEn;
        this.version = version;
        this.description = description;
    }

    public void setGuideFileIdEn(String guideFileIdEn) {
        if (guideFileIdEn == null || guideFileIdEn.equals("")) {
            this.guideFileIdEn = this.guideFileId;
        } else {
            this.guideFileIdEn = guideFileIdEn;
        }
    }

    public void setServiceEn(String serviceEn) {
        if (serviceEn == null || serviceEn.equals("")) {
            this.serviceEn = this.service;
        } else {
            this.serviceEn = serviceEn;
        }
    }
}