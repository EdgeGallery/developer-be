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

package org.edgegallery.developer.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.mapper.ApiEmulatorMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.workspace.ApiEmulator;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.template.ChartFileCreator;
import org.edgegallery.developer.template.CsarFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApiEmulatorMgr {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiEmulatorMgr.class);

    private static final String API_EMULATOR_K8S_TEMPLATE_FILE_PATH = "./configs/api_emulator_k8s_template.yaml";

    private static final String APP_NAME_PREFIX = "api-emulator-";

    private static final String CONFIG_NAME_PREFIX = "configmap-name-";

    private static final String NODE_PORT_STR = "{NODE_PORT}";

    private static final int MIN_PORT = 30400;

    private static final int MAX_PORT = 30500;

    @Autowired
    private ApiEmulatorMapper apiEmulatorMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private ProjectMapper projectMapper;

    private static String createEmulatorCsar(int nodePort) throws IOException {
        String appName = APP_NAME_PREFIX + nodePort;
        String configMapName = CONFIG_NAME_PREFIX + nodePort;
        ChartFileCreator chartFileCreator = new ChartFileCreator("apiEmulator");
        chartFileCreator.setChartValues("false", "false", "default", configMapName);
        String templateYamlContent = FileUtils
            .readFileToString(new File(API_EMULATOR_K8S_TEMPLATE_FILE_PATH), Consts.FILE_ENCODING)
            .replace(NODE_PORT_STR, Integer.toString(nodePort));
        chartFileCreator.addTemplateYaml("api-emulator.yaml", templateYamlContent);
        chartFileCreator.setChartName(appName);
        String tgzFilePath = chartFileCreator.build();
        CsarFileCreator csarFileCreator = new CsarFileCreator();
        csarFileCreator.setChartFilePath(tgzFilePath);
        csarFileCreator.setAppName(appName);
        return csarFileCreator.build();
    }

    /**
     * createApiEmulatorIfNotExist.
     */
    public void createApiEmulatorIfNotExist(String userId, String token) {
        if (userId == null || apiEmulatorMapper.getEmulatorByUserId(userId) != null) {
            return;
        }

        // select available host and nodePort
        MepHost host = selectAvailableHost();
        int nodePort = selectAvailablePortByHost(host.getHostId());
        String emulatorInstanceId = UUID.randomUUID().toString();

        // create csar
        String csarFilePath;
        try {
            csarFilePath = createEmulatorCsar(nodePort);
        } catch (IOException e) {
            LOGGER.error("Failed to create emulator csar file for user: {}", userId);
            return;
        }
        LOGGER.info("Succeed to create emulator csar file for user: {}", userId);
        Boolean instantiateAppResult = HttpClientUtil
            .instantiateApplication(host.getProtocol(), host.getIp(), host.getPort(), csarFilePath, emulatorInstanceId,
                userId, token,"testName");

        if (!instantiateAppResult) {
            LOGGER.error("Failed to instantiate emulator app for user: {}.", userId);
            return;
        }


        // remove csar file
        FileUtils.deleteQuietly(new File(csarFilePath));

        // save ApiEmulator
        String workloadId = UUID.randomUUID().toString();
        ApiEmulator apiEmulator = new ApiEmulator(emulatorInstanceId, userId, host.getHostId(), nodePort, workloadId);
        int saveResult = apiEmulatorMapper.saveEmulator(apiEmulator);
        if (saveResult != 1) {
            LOGGER.error("Failed to save emulator for user: {}, appInstanceId: {}, workload id: {}.", userId,
                emulatorInstanceId, workloadId);
            return;
        }
        LOGGER.info("Succeed to save emulator for user: {}, appInstanceId: {}, workload id: {}.", userId,
            emulatorInstanceId, workloadId);
    }

    private MepHost selectAvailableHost() {
        List<MepHost> normalHosts = hostMapper.getNormalHosts();
        return normalHosts.get(0);
    }

    private int selectAvailablePortByHost(String hostId) {
        int maxPort = apiEmulatorMapper.selectMaxPort(hostId);
        if (maxPort == 0 || maxPort == MAX_PORT) {
            return MIN_PORT;
        }
        return ++maxPort;
    }

    /**
     * deleteApiEmulatorIfProjectsNotExist.
     */
    public void deleteApiEmulatorIfProjectsNotExist(String userId, String token) {
        int projectsNum = projectMapper.countProjects(userId);
        ApiEmulator emulator = apiEmulatorMapper.getEmulatorByUserId(userId);

        if (projectsNum != 0 || emulator == null) {
            return;
        }
        String workloadId = emulator.getWorkloadId();
        String instanceId = emulator.getId();
        MepHost host = hostMapper.getHost(emulator.getHostId());
        boolean terminateResult = HttpClientUtil
                .terminateAppInstance(host.getProtocol(), host.getIp(), host.getPort(), instanceId, userId, token);
        if (!terminateResult) {
            LOGGER.error("Failed to terminate application which userId is: {}, instanceId is {}", userId, instanceId);
            return;
        }
        int deleteResult = apiEmulatorMapper.deleteEmulatorById(emulator.getId());
        if (deleteResult != 1) {
            LOGGER.error("Failed to delete emulator for user: {}, appInstanceId: {}, workload id: {}.", userId,
                instanceId, workloadId);
            return;
        }
        LOGGER.info("Succeed to delete emulator app for user: {}, appInstanceId: {}, workload id: {}.", userId,
            instanceId, workloadId);
    }
}