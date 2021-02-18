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

package org.edgegallery.developer.service;

import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.deployyaml.ConfigMap;
import org.edgegallery.developer.model.deployyaml.Containers;
import org.edgegallery.developer.model.deployyaml.DeployYaml;
import org.edgegallery.developer.model.deployyaml.DeployYamls;
import org.edgegallery.developer.model.deployyaml.Environment;
import org.edgegallery.developer.model.deployyaml.SecretKeyRef;
import org.edgegallery.developer.model.deployyaml.ServicePorts;
import org.edgegallery.developer.model.deployyaml.ValueFrom;
import org.edgegallery.developer.model.deployyaml.VolumeMounts;
import org.edgegallery.developer.model.deployyaml.Volumes;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("deployService")
public class DeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployService.class);

    private Gson gson = new Gson();

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private ProjectImageMapper projectImageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private AppReleaseService appReleaseService;

    public Either<FormatRespDto, UploadedFile> genarateDeployYaml(DeployYamls deployYamls, String projectId,
        String userId) throws IOException {
        if (deployYamls == null) {
            LOGGER.error("no request body param");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "no param"));
        }
        String fileId = UUID.randomUUID().toString();
        File filePath = new File(
            InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId + File.separator);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File yamlFile = new File(filePath + File.separator + "deploy.yaml");
        if (!yamlFile.exists()) {
            yamlFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(filePath + File.separator + "deploy.yaml", false);
        YamlWriter yamlWriter = new YamlWriter(fileWriter);
        yamlWriter.getConfig().writeConfig.setWriteRootTags(false); // 取消添加全限定类名
        yamlWriter.getConfig().writeConfig.setWriteRootElementTags(false);
        yamlWriter.getConfig().setAllowDuplicates(true);
        fileWriter.write("---\n"); // 分隔符

        List<OpenMepCapabilityGroup> capabilities = projectMapper.getProjectById(projectId).getCapabilityList();
        DeployYaml[] deploys = deployYamls.getDeployYamls();
        for (DeployYaml deployYaml : deploys) {
            if (deployYaml.getKind().equals("Pod")) {
                if (capabilities != null) {
                    Containers[] containers = deployYaml.getSpec().getContainers();
                    Containers[] copyContainers = new Containers[containers.length + 1];
                    for (int i = 0; i < containers.length; i++) {
                        copyContainers[i] = containers[i];
                    }
                    //add mep-agent container
                    Containers[] newContainers = insertMepAgent();
                    System.arraycopy(newContainers, 0, copyContainers, copyContainers.length - 1, newContainers.length);
                    deployYaml.getSpec().setContainers(copyContainers);
                    Volumes[] volumees = new Volumes[1];
                    Volumes volumes = new Volumes();
                    volumes.setName("mep-agent-service-config-volume");
                    ConfigMap configMap = new ConfigMap();
                    configMap.setName("{{ .Values.global.mepagent.configmapname }}");
                    volumes.setConfigMap(configMap);
                    volumees[0] = volumes;
                    deployYaml.getSpec().setVolumes(volumees);
                }
                yamlWriter.write(deployYaml);
            }
            if (deployYaml.getKind().equals("Service")) {
                yamlWriter.write(deployYaml);
            }
        }
        yamlWriter.close();
        //save pod and service info
        List<ProjectImageConfig> list = projectImageMapper.getAllImage(projectId);
        if (list != null) {
            projectImageMapper.deleteImage(projectId);
        }
        savePodAndService(deploys, projectId);
        //save deploy yaml
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFileId(fileId);
        uploadedFile.setFilePath(yamlFile.getCanonicalPath());
        uploadedFile.setFileName(yamlFile.getName());
        uploadedFile.setUserId(userId);
        uploadedFile.setTemp(false);
        uploadedFile.setUploadDate(new Date());
        int res = uploadedFileMapper.saveFile(uploadedFile);
        if (res <= 0) {
            LOGGER.error("save file failed!");
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save file failed!"));
        }
        uploadedFile.setFilePath("");
        return Either.right(uploadedFile);
    }

    /**
     * get yaml.
     *
     * @param fileId file id
     * @return
     */
    public Either<FormatRespDto, String> getDeployYaml(String fileId) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        if (uploadedFile != null) {
            if (!StringUtils.isEmpty(uploadedFile.getFilePath())) {
                String fileContent = appReleaseService.readFileIntoString(uploadedFile.getFilePath());
                return Either.right(fileContent);
            }
        }
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "fileId not exist!"));
    }

    /**
     * update yaml.
     *
     * @param fileId file id
     * @param content file cotent
     * @return
     */
    public Either<FormatRespDto, String> updateDeployYaml(String fileId, String content) {
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(fileId);
        //将content写进文件
        try {
            File file = new File(uploadedFile.getFilePath());
            FileWriter fw = new FileWriter(file.getCanonicalFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error("wirte new content into file,occur {}", e.getMessage());
            String msg = "wirte new content into file failed!";
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, msg));
        }
        //重新读取文件
        UploadedFile newFile = uploadedFileMapper.getFileById(fileId);
        String fileContent = appReleaseService.readFileIntoString(newFile.getFilePath());
        return Either.right(fileContent);
    }

    private void savePodAndService(DeployYaml[] deploys, String projectId) {
        List<DeployYaml> svcList = new ArrayList<>();
        List<DeployYaml> podList = new ArrayList<>();
        List<String> podNames = new ArrayList<>();
        List<String> svcNames = new ArrayList<>();
        for (DeployYaml deployYaml : deploys) {
            if (deployYaml.getKind().equals("Service")) {
                svcNames.add(deployYaml.getMetaData().getName());
                svcList.add(deployYaml);
            }
            if (deployYaml.getKind().equals("Pod")) {
                podNames.add(deployYaml.getMetaData().getName());
                podList.add(deployYaml);
            }
        }
        //no service
        if (svcList == null || svcList.size() == 0) {
            //只保存所有pod信息
            for (DeployYaml deployYaml : deploys) {
                ProjectImageConfig projectImageConfig = new ProjectImageConfig();
                projectImageConfig.setId(UUID.randomUUID().toString());
                projectImageConfig.setPodName(deployYaml.getMetaData().getName());
                Containers[] containersArr = deployYaml.getSpec().getContainers();
                projectImageConfig.setPodContainers(gson.toJson(containersArr));
                projectImageConfig.setProjectId(projectId);
                projectImageMapper.saveImage(projectImageConfig);
            }
        }

        //双重循环判断podname是否等于svcname
        for (int i = 0; i < podList.size(); i++) {
            for (int j = 0; j < svcList.size(); j++) {
                String podName = podList.get(i).getMetaData().getName();
                String svcName = svcList.get(j).getMetaData().getName();
                if (podName.equals(svcName)) {
                    ProjectImageConfig projectImageConfig = new ProjectImageConfig();
                    projectImageConfig.setId(UUID.randomUUID().toString());
                    projectImageConfig.setPodName(podName);
                    Containers[] containersArr = podList.get(i).getSpec().getContainers();
                    projectImageConfig.setPodContainers(gson.toJson(containersArr));
                    projectImageConfig.setProjectId(projectId);
                    projectImageConfig.setSvcType(svcList.get(j).getSpec().getType());
                    ServicePorts[] ports = svcList.get(j).getSpec().getPorts();
                    List<String> portList = new ArrayList<>();
                    List<String> nodePortList = new ArrayList<>();
                    for (ServicePorts servicePorts : ports) {
                        portList.add(Integer.toString(servicePorts.getPort()));
                        nodePortList.add(Integer.toString(servicePorts.getNodePort()));
                    }
                    projectImageConfig.setSvcPort(String.join(",", portList));
                    projectImageConfig.setSvcNodePort(String.join(",", nodePortList));
                    projectImageConfig.setSvcType(svcList.get(j).getSpec().getType());
                    projectImageMapper.saveImage(projectImageConfig);
                }
            }
        }
        //保存未匹配的pod
        saveUnmatchPod(podNames, svcNames, deploys, projectId);

    }

    private void saveUnmatchPod(List<String> podNames, List<String> svcNames, DeployYaml[] deployYamls,
        String projectId) {
        if (podNames.removeAll(svcNames)) {
            //获得差集
            podNames.removeAll(svcNames);
            //去除重复
            List<String> newNameList = removeStringListDupli(podNames);
            //只保存所有pod信息
            for (DeployYaml deployYaml : deployYamls) {
                for (String podName : newNameList) {
                    if (deployYaml.getMetaData().getName().equals(podName)) {
                        ProjectImageConfig projectImageConfig = new ProjectImageConfig();
                        projectImageConfig.setId(UUID.randomUUID().toString());
                        projectImageConfig.setPodName(deployYaml.getMetaData().getName());
                        Containers[] containersArr = deployYaml.getSpec().getContainers();
                        projectImageConfig.setPodContainers(gson.toJson(containersArr));
                        projectImageConfig.setProjectId(projectId);
                        projectImageMapper.saveImage(projectImageConfig);
                    }
                }
            }
        }
    }

    private List<String> removeStringListDupli(List<String> stringList) {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(stringList);
        stringList.clear();
        stringList.addAll(set);
        return stringList;
    }

    private Containers[] insertMepAgent() {
        Containers[] newContainers = new Containers[1];
        Containers containersMepAgent = new Containers();
        containersMepAgent.setName("mep-agent");
        containersMepAgent.setImage("mep-agent:latest");
        containersMepAgent.setImagePullPolicy("Always");
        Environment envWait = new Environment();
        envWait.setName("ENABLE_WAIT");
        envWait.setValue("true");
        Environment envMep = new Environment();
        envMep.setName("MEP_IP");
        envMep.setValue("mep-api-gw.mep");
        Environment envGate = new Environment();
        envGate.setName("MEP_APIGW_PORT");
        envGate.setValue("8443");
        Environment envName = new Environment();
        envName.setName("CA_CERT_DOMAIN_NAME");
        envName.setValue("edgegallery");
        Environment envCert = new Environment();
        envCert.setName("CA_CERT");
        envCert.setValue("/usr/mep/ssl/ca.crt");
        //Ak
        Environment envAk = new Environment();
        envAk.setName("AK");
        ValueFrom valueFrom = new ValueFrom();
        SecretKeyRef secretKeyRef = new SecretKeyRef();
        secretKeyRef.setName("{{ .Values.appconfig.aksk.secretname }}");
        secretKeyRef.setKey("accesskey");
        valueFrom.setSecretKeyRef(secretKeyRef);
        envAk.setValueFrom(valueFrom);
        //SK
        Environment envSk = new Environment();
        envSk.setName("SK");
        ValueFrom valueFromSK = new ValueFrom();
        SecretKeyRef secretKeyRefSK = new SecretKeyRef();
        secretKeyRefSK.setName("{{ .Values.appconfig.aksk.secretname }}");
        secretKeyRefSK.setKey("secretkey");
        valueFromSK.setSecretKeyRef(secretKeyRefSK);
        envSk.setValueFrom(valueFromSK);
        //APPINSTID
        Environment envApp = new Environment();
        envApp.setName("APPINSTID");
        ValueFrom valueFromApp = new ValueFrom();
        SecretKeyRef secretKeyRefApp = new SecretKeyRef();
        secretKeyRefApp.setName("{{ .Values.appconfig.aksk.secretname }}");
        secretKeyRefApp.setKey("appInsId");
        valueFromApp.setSecretKeyRef(secretKeyRefApp);
        envApp.setValueFrom(valueFromApp);
        Environment[] envs = new Environment[8];
        envs[0] = envWait;
        envs[1] = envMep;
        envs[2] = envGate;
        envs[3] = envName;
        envs[4] = envCert;
        envs[5] = envAk;
        envs[6] = envSk;
        envs[7] = envApp;
        containersMepAgent.setEnv(envs);
        //volumeMounts
        VolumeMounts[] volumeMounts = new VolumeMounts[1];
        VolumeMounts volumeMount = new VolumeMounts();
        volumeMount.setMountPath("/usr/mep/conf/app_instance_info.yaml");
        volumeMount.setName("mep-agent-service-config-volume");
        volumeMount.setSubPath("app_instance_info.yaml");
        volumeMounts[0] = volumeMount;
        containersMepAgent.setVolumeMounts(volumeMounts);
        newContainers[0] = containersMepAgent;
        return newContainers;
    }

}
