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
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.workspace.MepHost;

@Getter
@Setter
@ToString
public class VmCreateConfig {

    private String vmId;

    private String projectId;

    private String vmName;

    private EnumVmCreateStatus status;

    private VmCreateStageStatus stageStatus;

    private MepHost host;

    private String lcmToken;

    private List<VmInfo> vmInfo;

    private String appInstanceId;

    private String packageId;

    private Date createTime;

    private String log;

    public VmCreateConfig() {
    }

    /**
     * get next stage for deploy.
     */
    public String getNextStage() {
        if (this.getStageStatus() == null || this.getStageStatus().getHostInfo() == null) {
            return "hostInfo";
        } else if (this.getStageStatus().getDistributeInfo() == null) {
            return "distributeInfo";
        } else if (this.getStageStatus().getInstantiateInfo() == null) {
            return "instantiateInfo";
        } else if (this.getStageStatus().getWorkStatus() == null) {
            return "workStatus";
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

    /**
     * init config if necessary.
     */
    public void initialVmCreateConfig() {
        this.log = null;
        this.status = EnumVmCreateStatus.NOTCREATE;
        this.stageStatus = null;
        this.createTime = null;
        this.lcmToken = null;
        this.packageId = null;
        this.appInstanceId = null;
        this.vmInfo = null;
        this.host = null;
        this.vmName = null;

    }
}
