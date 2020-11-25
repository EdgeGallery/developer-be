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
import org.mec.developer.model.workspace.EnumDeployPlatform;

@Getter
@Setter
@ToString
public class ProjectTestConfig {

    /**
     * the uniqueId for ProjectTestConfig
     */
    private String testId;

    /**
     * projectId
     */
    private String projectId;

    /**
     * the platform where deploy
     */
    private EnumDeployPlatform platform;

    /**
     * the id of deploy file
     */
    private String deployFileId;

    /**
     * if privateHost is true, deploy project with local mep host
     */
    private boolean privateHost;

    /**
     * the pod list from mep after exec deploy job
     */
    private List<String> pods = new ArrayList<>();

    /**
     * the deploy status for this test_config
     */
    private EnumTestConfigDeployStatus deployStatus;

    /**
     * the exec stage status under deploying
     */
    private ProjectTestConfigStageStatus stageStatus;

    /**
     * the mep host list for deploy
     */
    private List<MepHost> hosts;

    /**
     * create errorLog when deploying
     */
    private String errorLog;

    /**
     * get workLoadId at deploying stage: workStatus
     */
    private String workLoadId;

    /**
     * get appInstanceId at deploying stage:instantiateInfo
     */
    private String appInstanceId;

    /**
     * get appInstanceId after deploy success
     */
    private Date deployDate;

    /**
     * the tmp token for call lcm
     */
    private String lcmToken;

    /**
     * the number of deploying retry
     */
    private int retry;

    // yaml config
    private MepAgentConfig agentConfig;

    private List<String> imageFileIds;

    // this URL is the image in repo
    private List<CommonImage> appImages;

    private List<CommonImage> otherImages;

    /**
     *  the swagger api file id
     */
    private String appApiFileId;

    private String accessUrl;


    /**
     *  get next stage for deploy
     */
    public String getNextStage(){
        if (this.getStageStatus() == null || this.getStageStatus().getCsar() == null ){
            return "csar";
        } else if (this.getStageStatus().getHostInfo() == null){
            return "hostInfo";
        } else if (this.getStageStatus().getInstantiateInfo() ==null){
            return "instantiateInfo";
        } else if (this.getStageStatus().getWorkStatus() == null){
            return "workStatus";
        }
        return null;
    }

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
     * init config if necessary。
     */
    public void initialConfig(){
        this.pods = null;
        this.deployStatus = EnumTestConfigDeployStatus.NOTDEPLOY;
        this.stageStatus = null;
        this.workLoadId = null;
        this.appInstanceId = null;
        this.lcmToken = null;
        this.accessUrl = null;
        this.deployDate = null;
        this.errorLog = null;
        this.retry = 0;
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
