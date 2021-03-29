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

package org.edgegallery.developer.model.vm;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmImageConfig {

    private String vmId;

    private String imageId;

    private String projectId;

    private String vmName;

    private String imageName;

    private String appInstanceId;

    private String hostIp;

    private Integer sumChunkNum;

    private Integer chunkSize;

    private VmImportStageStatus stageStatus;

    private EnumVmImportStatus status;

    private String lcmToken;

    private Date createTime;

    private String log;

    public VmImageConfig() {
    }

    /**
     * get next stage for deploy.
     */
    public String getNextStage() {
        if (this.getStageStatus() == null || this.getStageStatus().getCreateImageInfo() == null) {
            return "createImageInfo";
        } else if (this.getStageStatus().getImageStatus() == null) {
            return "imageStatus";
        } else if (this.getStageStatus().getDownloadImageInfo() == null) {
            return "downloadImageInfo";
        }
        return null;
    }

    /**
     * getCreateTime.
     *
     * @return
     */
    public Date getCreateTime() {
        if (createTime != null) {
            return (Date) createTime.clone();
        }
        return null;
    }

    /**
     * setCreateTime.
     *
     * @param createTime createTime
     */
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = (Date) createTime.clone();
        } else {
            this.createTime = null;
        }

    }

}
