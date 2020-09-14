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

package org.edgegallery.developer.service.csar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.CommonImage;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;

public class CreateCsarFromTemplate {

    private static final String[] simpleFiles = {
        "/MainServiceTemplate.mf",
        "/TOSCA-Metadata/TOSCA.meta",
        "/Definitions/MainServiceTemplate.yaml",
        "/Artifacts/Deployment/Charts/app-tgz/Chart.yaml",
        "/Artifacts/Deployment/Charts/app-tgz/values.yaml",
        "/Artifacts/Deployment/Charts/app-tgz/templates/configmap.yaml",
        "/Artifacts/Deployment/Charts/app-tgz/templates/pod.yaml",
        "/Artifacts/Deployment/Charts/app-tgz/templates/service.yaml"
    };

    private static final String[] simpleFilesWithoutChart = {
        "/MainServiceTemplate.mf",
        "/TOSCA-Metadata/TOSCA.meta",
        "/Definitions/MainServiceTemplate.yaml",
    };


    private static final String WORKSPACE_CSAR_PATH = "./configs/workspace_csar";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/Artifacts/Deployment/Charts/";

    /**
     * create csar.
     *
     * @param projectPath path of project
     * @param config test config of project
     * @param project project self
     * @return package gz
     */
    public File create(String projectPath, ProjectTestConfig config, ApplicationProject project, File chart)
        throws IOException {
        File projectDir = new File(projectPath);

        // copy template files to the new project path
        File csar = DeveloperFileUtils.copyDirAndReName(new File(WORKSPACE_CSAR_PATH), projectDir, project.getId());

        // get data to Map<String, String>
        String timeStamp = String.valueOf(new Date().getTime());
        Map<String, String> values = initValueMap(config, project, timeStamp);

        // modify the csar files and fill in the data
        List<CreateCsarFileHanlder> hanlders = new ArrayList<>();
        String[] files = simpleFiles;
        if (chart != null) {
            files = simpleFilesWithoutChart;
        }
        for (String file : files) {
            CreateCsarFileHanlder hanlder = new CreateCsarFileHanlder();
            hanlder.setFilePath(csar.getCanonicalPath() + file);
            hanlder.setFillData(values);
            hanlders.add(hanlder);
        }

        for (CreateCsarFileHanlder hanlder : hanlders) {
            hanlder.execute();
        }

        if (chart != null) {
            // move chart.tgz to Chart directory and delete apptgz dir
            File chartDir = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH);
            FileUtils.cleanDirectory(chartDir);
            FileUtils.moveFileToDirectory(chart, chartDir, true);
        } else {
            //compose apptgz to .tgz and delete apptgz dir
            String appName = project.getName() + timeStamp;
            File appTgz = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH + "app-tgz/");
            File appTgzNew = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH, appName);
            if (!appTgz.renameTo(appTgzNew)) {
                throw new IOException("Rename tgz exception");
            }
            File tgz = CompressFileUtils
                .compressToTgzAndDeleteSrc(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH + appName,
                    csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH, appName);
            if (!tgz.exists()) {
                throw new IOException("Create tgz exception");
            }
        }


        return csar;
    }

    private Map<String, String> initValueMap(ProjectTestConfig config, ApplicationProject project, String timeStamp) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("\\{name}", project.getName());
        valueMap.put("\\{version}", project.getVersion());
        valueMap.put("\\{provider}", project.getProvider());
        valueMap.put("\\{id}", UUID.randomUUID().toString());
        valueMap.put("\\{time}", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()) + "+00:00");
        valueMap.put("\\{timeStamp}", timeStamp);

        int imageCount = 0;
        for (CommonImage appImage : config.getAppImages()) {
            imageCount++;
            valueMap.put("imageName" + imageCount, appImage.getImageName());
            valueMap.put("imageVersion" + imageCount, appImage.getVersion());
            valueMap.put("imageNodePort" + imageCount, String.valueOf(appImage.getPorts().get(0).getNodePort()));
            valueMap
                .put("imageContainerPort" + imageCount, String.valueOf(appImage.getPorts().get(0).getContainerPort()));
        }
        valueMap.put("imageCount", String.valueOf(imageCount));
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        Gson gson = new Gson();
        List<MepHost> hosts = gson.fromJson(gson.toJson(config.getHosts()), type);
        valueMap.put("\\{hostIp}", hosts.get(0).getIp());

        valueMap.put("\\{appInstanceId}", UUID.randomUUID().toString());
        valueMap.put("\\{agentName}",
            config.getAgentConfig().getServiceName().replaceAll(Consts.PATTERN, "-").toLowerCase());
        valueMap.put("\\{agentPort}", String.valueOf(config.getAgentConfig().getPort()));

        return valueMap;
    }

}
