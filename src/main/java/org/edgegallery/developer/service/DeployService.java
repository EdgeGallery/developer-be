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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.entity.ContentType;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.deployyaml.ConfigMap;
import org.edgegallery.developer.model.deployyaml.Containers;
import org.edgegallery.developer.model.deployyaml.DeployYaml;
import org.edgegallery.developer.model.deployyaml.DeployYamls;
import org.edgegallery.developer.model.deployyaml.Environment;
import org.edgegallery.developer.model.deployyaml.PodImage;
import org.edgegallery.developer.model.deployyaml.SecretKeyRef;
import org.edgegallery.developer.model.deployyaml.ServicePorts;
import org.edgegallery.developer.model.deployyaml.ValueFrom;
import org.edgegallery.developer.model.deployyaml.VolumeMounts;
import org.edgegallery.developer.model.deployyaml.Volumes;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("deployService")
public class DeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployService.class);

    private Gson gson = new Gson();

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ProjectImageMapper projectImageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private AppReleaseService appReleaseService;

    public Either<FormatRespDto, HelmTemplateYamlPo> saveDeployYaml(String jsonstr, String projectId, String userId,
        String configType) throws IOException {
        if (StringUtils.isEmpty(jsonstr)) {
            LOGGER.error("no request body param");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "no param"));
        }
        String reqContentnew = jsonstr.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "").trim();
        String[] reqs = reqContentnew.split("\\{\"apiVersion\"");
        //save pod
        List<String> sbPod = new ArrayList<>();
        List<String> sbService = new ArrayList<>();
        for (int i = 0; i < reqs.length; i++) {
            if (reqs[i].contains("\"Pod\"")) {
                sbPod.add("\\{\"apiVersion\"" + reqs[i].substring(0, reqs[i].length() - 1));
            }
            if (reqs[i].contains("\"Service\"")) {
                if (reqs[i].endsWith("}}]")) {
                    sbService.add("\\{\"apiVersion\"" + reqs[i].substring(0, reqs[i].length() - 1));
                } else {
                    sbService.add("\\{\"apiVersion\"" + reqs[i].substring(0, reqs[i].length() - 1));
                }
            }
        }

        //judge mep
        List<OpenMepCapabilityGroup> list = projectMapper.getProjectById(projectId).getCapabilityList();
        //save service
        StringBuilder sb = new StringBuilder();
        List<String> podName = new ArrayList<>();
        List<String> podImages = new ArrayList<>();
        for (int h = 0; h < sbPod.size(); h++) {
            //get podName and image
            String jsonPod = sbPod.get(h).substring(1);
            DeployYaml deployYaml = new Gson().fromJson(jsonPod, DeployYaml.class);
            if (!CollectionUtils.isEmpty(list) && h == 0) {
                jsonPod = addMepAgent(deployYaml);
            }
            podName.add(deployYaml.getMetadata().getName());
            PodImage podIm = new PodImage();
            podIm.setPodName(deployYaml.getMetadata().getName());
            Containers[] containers = deployYaml.getSpec().getContainers();
            String[] images = new String[containers.length];
            for (int i = 0; i < containers.length; i++) {
                if (null != containers[i]) {
                    images[i] = containers[i].getImage();
                }
            }
            podIm.setPodImage(images);
            podImages.add(new Gson().toJson(podIm));
            //convert to yaml
            JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonPod);
            // save it as YAML
            String jsonAsYaml = new YAMLMapper().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
                .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true).writeValueAsString(jsonNodeTree);
            sb.append(jsonAsYaml);
        }
        StringBuilder sbs = new StringBuilder();
        List<String> svcType = new ArrayList<>();
        List<String> svcPort = new ArrayList<>();
        List<String> svcNodePort = new ArrayList<>();
        for (String svc : sbService) {
            //get svcType/svcPort/svcNodePort
            DeployYaml deployYaml = new Gson().fromJson(svc.substring(1), DeployYaml.class);
            ServicePorts[] ports = deployYaml.getSpec().getPorts();
            svcType.add(deployYaml.getSpec().getType());
            for (ServicePorts servicePorts : ports) {
                svcNodePort.add(String.valueOf(servicePorts.getNodePort()));
                svcPort.add(String.valueOf(servicePorts.getPort()));
            }
            //convert svc to yaml
            JsonNode jsonNodeTree = new ObjectMapper().readTree(svc.substring(1));
            // save it as YAML
            String jsonAsYaml = new YAMLMapper().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
                .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true).writeValueAsString(jsonNodeTree);
            sbs.append(jsonAsYaml);
        }

        String yamlContent = sb.toString() + sbs.toString();
        HelmTemplateYamlPo helmPo = new HelmTemplateYamlPo();
        String fileId = UUID.randomUUID().toString();
        helmPo.setFileId(fileId);
        helmPo.setFileName("deploy.yaml");
        helmPo.setContent(yamlContent);
        helmPo.setUserId(userId);
        helmPo.setProjectId(projectId);
        helmPo.setConfigType(configType);
        helmPo.setUploadTimeStamp(System.currentTimeMillis());
        int resHelm = helmTemplateYamlMapper.saveYaml(helmPo);
        if (resHelm <= 0) {
            Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "save deploy failed!"));
        }

        //save image
        ProjectImageConfig imageConfig = new ProjectImageConfig();
        imageConfig.setId(UUID.randomUUID().toString());
        imageConfig.setProjectId(projectId);
        imageConfig.setPodName(podName.toString());
        imageConfig.setPodContainers(podImages.toString());
        imageConfig.setSvcType(svcType.toString());
        imageConfig.setSvcNodePort(svcNodePort.toString());
        imageConfig.setSvcPort(svcPort.toString());
        int resImage = projectImageMapper.saveImage(imageConfig);
        if (resImage <= 0) {
            Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "save image failed!"));
        }
        return Either.right(helmTemplateYamlMapper.queryTemplateYamlById(fileId));
    }


    public String addMepAgent(DeployYaml deployYaml){
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
        return new Gson().toJson(deployYaml);
    }

    public Either<FormatRespDto, HelmTemplateYamlRespDto> genarateDeployYaml(DeployYamls deployYamls, String projectId,
        String userId, String configType) throws IOException {
        if (deployYamls == null) {
            LOGGER.error("no request body param");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "no param"));
        }
        // String fileId = UUID.randomUUID().toString();
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
        for (int j = 0; j < deploys.length; j++) {
            if (deploys[j].getKind().equals("Pod")) {
                if (!CollectionUtils.isEmpty(capabilities) && j == 0) {
                    Containers[] containers = deploys[j].getSpec().getContainers();
                    Containers[] copyContainers = new Containers[containers.length + 1];
                    for (int i = 0; i < containers.length; i++) {
                        copyContainers[i] = containers[i];
                    }
                    //add mep-agent container
                    Containers[] newContainers = insertMepAgent();
                    System.arraycopy(newContainers, 0, copyContainers, copyContainers.length - 1, newContainers.length);
                    deploys[j].getSpec().setContainers(copyContainers);
                    Volumes[] volumees = new Volumes[1];
                    Volumes volumes = new Volumes();
                    volumes.setName("mep-agent-service-config-volume");
                    ConfigMap configMap = new ConfigMap();
                    configMap.setName("{{ .Values.global.mepagent.configmapname }}");
                    volumes.setConfigMap(configMap);
                    volumees[0] = volumes;
                    deploys[j].getSpec().setVolumes(volumees);
                }
                yamlWriter.write(deploys[j]);
            }
            if (deploys[j].getKind().equals("Service")) {
                yamlWriter.write(deploys[j]);
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
        InputStream inputStream = new FileInputStream(yamlFile);
        MultipartFile multipartFile = new MockMultipartFile(yamlFile.getName(), yamlFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
        Either<FormatRespDto, HelmTemplateYamlRespDto> res = uploadFileService
            .uploadHelmTemplateYaml(multipartFile, userId, projectId, configType);
        if (res.isLeft()) {
            return Either.left(res.getLeft());
        }
        return Either.right(res.getRight());
    }

    /**
     * update yaml.
     *
     * @param fileId file id
     * @param content file cotent
     * @return
     */
    public Either<FormatRespDto, HelmTemplateYamlPo> updateDeployYaml(String fileId, String content) {
        HelmTemplateYamlPo helmPo = helmTemplateYamlMapper.queryTemplateYamlById(fileId);
        helmPo.setContent(content);
        int res = helmTemplateYamlMapper.updateHelm(helmPo);
        //save deploy yaml
        if (res <= 0) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "update yaml failed"));
        }
        return Either.right(helmTemplateYamlMapper.queryTemplateYamlById(fileId));
    }

    /**
     * get yaml.
     *
     * @return
     */
    public Either<FormatRespDto, HelmTemplateYamlPo> getDeployYamlContent(String fileId) {
        HelmTemplateYamlPo helmPo = helmTemplateYamlMapper.queryTemplateYamlById(fileId);
        if (helmPo != null) {
            return Either.right(helmPo);
        }
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any content!"));
    }

    /**
     * get yaml.
     *
     * @return
     */
    public Either<FormatRespDto, List<String>> getDeployYamJson(String fileId) throws JsonProcessingException {
        HelmTemplateYamlPo helmPo = helmTemplateYamlMapper.queryTemplateYamlById(fileId);
        if (helmPo != null) {
            String content = helmPo.getContent();
            List<String> list = new ArrayList<>();
            String[] ys = content.split("---");
            for (int m = 0; m < ys.length; m++) {
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(ys[m])) {
                    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                    Object obj = yamlReader.readValue(ys[m], Object.class);
                    ObjectMapper jsonWriter = new ObjectMapper();
                    list.add(jsonWriter.writeValueAsString(obj));
                }
            }
            return Either.right(list);
        }
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any content!"));
    }

    private void savePodAndService(DeployYaml[] deploys, String projectId) {
        List<DeployYaml> svcList = new ArrayList<>();
        List<DeployYaml> podList = new ArrayList<>();
        List<String> podNames = new ArrayList<>();
        List<String> svcNames = new ArrayList<>();
        for (DeployYaml deployYaml : deploys) {
            if (deployYaml.getKind().equals("Service")) {
                svcNames.add(deployYaml.getMetadata().getName());
                svcList.add(deployYaml);
            }
            if (deployYaml.getKind().equals("Pod")) {
                podNames.add(deployYaml.getMetadata().getName());
                podList.add(deployYaml);
            }
        }
        //no service
        if (svcList == null || svcList.size() == 0) {
            //只保存所有pod信息
            for (DeployYaml deployYaml : deploys) {
                ProjectImageConfig projectImageConfig = new ProjectImageConfig();
                projectImageConfig.setId(UUID.randomUUID().toString());
                projectImageConfig.setPodName(deployYaml.getMetadata().getName());
                Containers[] containersArr = deployYaml.getSpec().getContainers();
                projectImageConfig.setPodContainers(gson.toJson(containersArr));
                projectImageConfig.setProjectId(projectId);
                projectImageMapper.saveImage(projectImageConfig);
            }
        }

        //双重循环判断podname是否等于svcname
        for (int i = 0; i < podList.size(); i++) {
            for (int j = 0; j < svcList.size(); j++) {
                String podName = podList.get(i).getMetadata().getName();
                String svcName = svcList.get(j).getMetadata().getName();
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
                    if (deployYaml.getMetadata().getName().equals(podName)) {
                        ProjectImageConfig projectImageConfig = new ProjectImageConfig();
                        projectImageConfig.setId(UUID.randomUUID().toString());
                        projectImageConfig.setPodName(deployYaml.getMetadata().getName());
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
        envWait.setValue("\"true\"");
        Environment envMep = new Environment();
        envMep.setName("MEP_IP");
        envMep.setValue("\"mep-api-gw.mep\"");
        Environment envGate = new Environment();
        envGate.setName("MEP_APIGW_PORT");
        envGate.setValue("\"8443\"");
        Environment envName = new Environment();
        envName.setName("CA_CERT_DOMAIN_NAME");
        envName.setValue("\"edgegallery\"");
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
