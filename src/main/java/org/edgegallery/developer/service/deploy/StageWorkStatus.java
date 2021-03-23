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

package org.edgegallery.developer.service.deploy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.deployyaml.PodEvents;
import org.edgegallery.developer.model.deployyaml.PodEventsRes;
import org.edgegallery.developer.model.deployyaml.PodStatusInfo;
import org.edgegallery.developer.model.deployyaml.PodStatusInfos;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * StageWorkStatus.
 *
 * @author chenhui
 */
@Service("workStatus_service")
public class StageWorkStatus implements IConfigDeployStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageWorkStatus.class);

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 360L;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean execute(ProjectTestConfig config) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(config.getHosts()), type);
        MepHost host = hosts.get(0);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(), userId,
                config.getLcmToken());
        String workEvents = HttpClientUtil
            .getWorkloadEvents(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(), userId,
                config.getLcmToken());
        if (workStatus == null || workEvents == null) {
            // compare time between now and deployDate
            long time = System.currentTimeMillis() - config.getDeployDate().getTime();
            LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS,
                config.getDeployDate().getTime());
            if (config.getDeployDate() == null || time > MAX_SECONDS * 1000) {
                config.setErrorLog("Failed to get workloadStatus: pull images failed ");
                String message = "Failed to get workloadStatus after wait {} seconds which appInstanceId is : {}";
                LOGGER.error(message, MAX_SECONDS, config.getAppInstanceId());
            } else {
                return true;
            }
        } else {
            processStatus = true;
            status = EnumTestConfigStatus.Success;
            //merge workStatus and workEvents
            String pods = mergeStatusAndEvents(workStatus, workEvents);
            config.setPods(pods);
            LOGGER.info("Query workload status response: {}", workStatus);
        }
        // update test-config
        projectService.updateDeployResult(config, project, "workStatus", status);
        return processStatus;
    }

    private String mergeStatusAndEvents(String workStatus, String workEvents) {
        Gson gson = new Gson();
        Type type = new TypeToken<PodStatusInfos>() { }.getType();
        PodStatusInfos status = gson.fromJson(workStatus, type);

        Type typeEvents = new TypeToken<PodEventsRes>() { }.getType();
        PodEventsRes events = gson.fromJson(workEvents, typeEvents);
        String pods = "";
        if (!CollectionUtils.isEmpty(status.getPods()) && !CollectionUtils.isEmpty(events.getPods())) {
            List<PodStatusInfo> statusInfos = status.getPods();
            List<PodEvents> eventsInfos = events.getPods();
            for (int i = 0; i < statusInfos.size(); i++) {
                for (int j = 0; j < eventsInfos.size(); j++) {
                    if (statusInfos.get(i).getPodname().equals(eventsInfos.get(i).getPodName())) {
                        statusInfos.get(i).setPodEventsInfo(eventsInfos.get(i).getPodEventsInfo());
                    }
                }
            }
            pods = gson.toJson(status);
        }
        return pods;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public boolean immediateExecute(ProjectTestConfig config) {
        return true;
    }
}
