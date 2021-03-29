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

package org.edgegallery.developer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringEscapeUtils;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.deployyaml.ConfigMap;
import org.edgegallery.developer.model.deployyaml.Containers;
import org.edgegallery.developer.model.deployyaml.DeployYaml;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service("deployService")
public class DeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployService.class);

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private ProjectImageMapper projectImageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * saveDeployYaml.
     */
    public Either<FormatRespDto, HelmTemplateYamlPo> saveDeployYaml(String jsonstr, String projectId, String userId,
        String configType) throws IOException {
        if (StringUtils.isEmpty(jsonstr)) {
            LOGGER.error("no request body param");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "no param"));
        }
        String reqContentnew = jsonstr.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "").trim();
        String env = "\"env\":[{\"name\":\"\",\"value\":\"\"}],";
        String command = "\"command\":\"[\\\\\\\"\\\\\\\"]\",";
        String resources =
            ",\"resources\":\\{\"limits\":\\{\"memory\":\"\",\"cpu\":\"\"},\"requests\":\\{\"memory\":\"\","
                + "\"cpu\":\"\"}}";
        if (reqContentnew.contains(env)) {
            reqContentnew = reqContentnew.replace(env, "");
        }
        if (reqContentnew.contains(command)) {
            reqContentnew = reqContentnew.replace(command, "");
        }
        if (reqContentnew.contains(StringEscapeUtils.unescapeJava(resources))) {
            reqContentnew = reqContentnew.replace(StringEscapeUtils.unescapeJava(resources), "");
        }
        String[] reqs = reqContentnew.trim().split("\\{\"apiVersion\"");
        //save pod
        List<String> sbPod = new ArrayList<>();
        List<String> sbService = new ArrayList<>();
        for (int i = 0; i < reqs.length; i++) {
            if (reqs[i].contains("\"Pod\"")) {
                sbPod.add("\\{\"apiVersion\"" + reqs[i].substring(0, reqs[i].length() - 1));
            }
            if (reqs[i].contains("\"Service\"")) {
                sbService.add("\\{\"apiVersion\"" + reqs[i].substring(0, reqs[i].length() - 1));
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
        //delete all helm by projectId
        List<HelmTemplateYamlPo> listPo = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
        if (!CollectionUtils.isEmpty(listPo)) {
            for (HelmTemplateYamlPo po : listPo) {
                helmTemplateYamlMapper.deleteYamlByFileId(po.getFileId());
            }
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

        List<ProjectImageConfig> listImage = projectImageMapper.getAllImage(projectId);
        if (!CollectionUtils.isEmpty(listImage)) {
            for (ProjectImageConfig po : listImage) {
                projectImageMapper.deleteImage(po.getProjectId());
            }
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

    /**
     * addMepAgent.
     */
    private String addMepAgent(DeployYaml deployYaml) {
        Containers[] containers = deployYaml.getSpec().getContainers();
        Containers[] copyContainers = new Containers[containers.length + 1];
        for (int i = 0; i < containers.length; i++) {
            copyContainers[i] = containers[i];
        }
        //add mep-agent container
        Containers[] newContainers = insertMepAgent();
        System.arraycopy(newContainers, 0, copyContainers, copyContainers.length - 1, newContainers.length);
        deployYaml.getSpec().setContainers(copyContainers);
        Volumes volumes = new Volumes();
        volumes.setName("mep-agent-service-config-volume");
        ConfigMap configMap = new ConfigMap();
        configMap.setName("{{ .Values.global.mepagent.configmapname }}");
        volumes.setConfigMap(configMap);
        Volumes[] volumees = new Volumes[1];
        volumees[0] = volumes;
        deployYaml.getSpec().setVolumes(volumees);
        return new Gson().toJson(deployYaml);
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

    private Containers[] insertMepAgent() {
        Containers containersMepAgent = new Containers();
        containersMepAgent.setName("mep-agent");
        containersMepAgent
            .setImage("{{.Values.imagelocation.domainname}}/{{.Values.imagelocation.project}}/mep-agent:latest");
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
        VolumeMounts volumeMount = new VolumeMounts();
        volumeMount.setMountPath("/usr/mep/conf/app_instance_info.yaml");
        volumeMount.setName("mep-agent-service-config-volume");
        volumeMount.setSubPath("app_instance_info.yaml");
        VolumeMounts[] volumeMounts = new VolumeMounts[1];
        volumeMounts[0] = volumeMount;
        containersMepAgent.setVolumeMounts(volumeMounts);
        Containers[] newContainers = new Containers[1];
        newContainers[0] = containersMepAgent;
        return newContainers;
    }

}
