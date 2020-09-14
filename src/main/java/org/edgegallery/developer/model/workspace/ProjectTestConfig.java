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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectTestConfig {

    private String testId;

    private String projectId;

    // yaml config
    private MepAgentConfig agentConfig;

    private List<String> imageFileIds;

    // this URL is the image in repo
    private List<CommonImage> appImages;

    private List<CommonImage> otherImages;

    private List<MepHost> hosts;

    private String appApiFileId;

    // when deployed, will fill the three parameters.
    private EnumTestStatus status;

    private String accessUrl;

    private String errorLog;

    private String workLoadId;

    private String appInstanceId;

    private Date deployDate;

    /**
     * getTestId.
     */
    public String getTestId() {
        if (this.testId == null) {
            this.testId = UUID.randomUUID().toString();
        }
        return this.testId;
    }

    /**
     * addAppImages.
     */
    public void addAppImages(CommonImage image) {
        if (appImages == null) {
            appImages = new ArrayList<>();
        }
        appImages.add(image);
    }

    /**
     * addOtherImages.
     */
    public void addOtherImages(CommonImage image) {
        if (otherImages == null) {
            otherImages = new ArrayList<>();
        }
        otherImages.add(image);
    }

    /**
     * getDeployDate.
     */
    public Date getDeployDate() {
        if (this.deployDate != null) {
            return new Date(this.deployDate.getTime());
        } else {
            return null;
        }
    }

    /**
     * setDeployDate.
     */
    public void setDeployDate(Date deployDate) {
        if (deployDate != null) {
            this.deployDate = (Date) deployDate.clone();
        } else {
            this.deployDate = null;
        }
    }
}
