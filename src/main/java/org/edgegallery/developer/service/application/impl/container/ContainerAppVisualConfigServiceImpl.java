package org.edgegallery.developer.service.application.impl.container;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.application.container.ContainerAppImageInfoMapper;
import org.edgegallery.developer.mapper.application.container.HelmChartMapper;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.model.application.container.ContainerAppImageInfo;
import org.edgegallery.developer.model.application.container.ContainerAppPodInfo;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.deployyaml.ConfigMap;
import org.edgegallery.developer.model.deployyaml.Containers;
import org.edgegallery.developer.model.deployyaml.DeployYaml;
import org.edgegallery.developer.model.deployyaml.Environment;
import org.edgegallery.developer.model.deployyaml.SecretKeyRef;
import org.edgegallery.developer.model.deployyaml.ValueFrom;
import org.edgegallery.developer.model.deployyaml.VolumeMounts;
import org.edgegallery.developer.model.deployyaml.Volumes;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.service.application.container.ContainerAppVisualConfigService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service("containerAppVisualConfigService")
public class ContainerAppVisualConfigServiceImpl implements ContainerAppVisualConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppVisualConfigServiceImpl.class);

    @Autowired
    private CapabilityMapper capabilityMapper;

    @Autowired
    private HelmChartMapper helmChartMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private ContainerAppImageInfoMapper appImageInfoMapper;

    @Override
    public HelmChart saveDeployYaml(String configJsonData, String applicationId, String configType) {
        if (StringUtils.isEmpty(configJsonData)) {
            LOGGER.error("no request body param");
            throw new IllegalRequestException("no request body param", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        //Processing is not required. If it is not filled in, it will be deleted in the generated yaml
        String newJsonData = configJsonData.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "").trim();
        String handledData = handleNonRequiredItems(newJsonData);
        String[] jsonDatas = handledData.trim().split("\\{\"apiVersion\"");
        //Process the pod and service that need to be saved into the data table
        return handlePodAndService(jsonDatas, applicationId);
    }

    @Override
    public String updateDeployYaml(String yamlFileId, String yamlContent) {
        if (StringUtils.isEmpty(yamlFileId)) {
            LOGGER.error("yaml id is empty!");
            throw new IllegalRequestException("yaml id is empty.", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(yamlFileId);
        if (uploadedFile == null) {
            LOGGER.error("query object(UploadedFile) is null!");
            throw new DataBaseException("query object(UploadedFile) is null.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String filePath = InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath();
        File deployYaml = new File(filePath);
        String content = "";
        List<String> list = null;
        try {
            FileUtils.writeStringToFile(deployYaml, yamlContent, StandardCharsets.UTF_8);
            content = FileUtils.readFileToString(deployYaml, StandardCharsets.UTF_8);
            list = FileUtils.readLines(deployYaml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("write or read file occur {}", e.getMessage());
            return null;
        }
        //update image info
        updateImageInfo(list, yamlFileId);
        return content;
    }

    @Override
    public String getDeployYaml(String yamlFileId) {
        if (StringUtils.isEmpty(yamlFileId)) {
            LOGGER.error("yaml id is empty!");
            throw new IllegalRequestException("yaml id is empty.", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        UploadedFile uploadedFile = uploadedFileMapper.getFileById(yamlFileId);
        if (uploadedFile == null) {
            LOGGER.error("query object(UploadedFile) is null!");
            throw new DataBaseException("query object(UploadedFile) is null.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String filePath = InitConfigUtil.getWorkSpaceBaseDir() + uploadedFile.getFilePath();
        String content = "";
        try {
            content = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("read file to string occur {}", e.getMessage());
            return null;
        }
        return content;
    }

    @Override
    public List<String> getDeployYamlAsList(String yamlFileId) {
        if (StringUtils.isEmpty(yamlFileId)) {
            LOGGER.error("yaml id is empty!");
            throw new IllegalRequestException("yaml id is empty.", ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
        String content = getDeployYaml(yamlFileId);
        if (StringUtils.isEmpty(content)) {
            LOGGER.error("yaml is empty!");
            throw new FileOperateException("yaml is empty.", ResponseConsts.RET_READ_FILE_FAIL);
        }
        List<String> list = new ArrayList<>();
        try {
            String[] yamlArr = content.split("---");
            for (int i = 0; i < yamlArr.length; i++) {
                if (!StringUtils.isEmpty(yamlArr[i])) {
                    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                    Object obj = yamlReader.readValue(yamlArr[i], Object.class);
                    ObjectMapper jsonWriter = new ObjectMapper();
                    list.add(jsonWriter.writeValueAsString(obj));
                }
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("get yaml data as list,occur {}", e.getMessage());
            return Collections.emptyList();
        }
        return list;
    }

    private String handleNonRequiredItems(String configJsonData) {
        String env = "\"env\":[{\"name\":\"\",\"value\":\"\"}],";
        String command = "\"command\":\"[\\\\\\\"\\\\\\\"]\",";
        String resources =
            ",\"resources\":\\{\"limits\":\\{\"memory\":\"\",\"cpu\":\"\"},\"requests\":\\{\"memory\":\"\","
                + "\"cpu\":\"\"}}";
        if (configJsonData.contains(env)) {
            configJsonData = configJsonData.replace(env, "");
        }
        if (configJsonData.contains(command)) {
            configJsonData = configJsonData.replace(command, "");
        }
        if (configJsonData.contains(StringEscapeUtils.unescapeJava(resources))) {
            configJsonData = configJsonData.replace(StringEscapeUtils.unescapeJava(resources), "");
        }
        return configJsonData;
    }

    private void updateImageInfo(List<String> list, String yamlFileId) {
        List<String> pods = new ArrayList<>();
        for (String image : list) {
            if (image.contains("image:")) {
                pods.add(image.substring(12, image.length() - 1));
            }
        }
        LOGGER.warn("pods:{}",pods);
        int resImage = appImageInfoMapper.updateImageInfo(pods.toString(), yamlFileId);
        if (resImage <= 0) {
            throw new DataBaseException("update image failed!", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
    }

    private HelmChart handlePodAndService(String[] jsonDatas, String applicationId) {
        List<String> pods = new ArrayList<>();
        List<String> svcs = new ArrayList<>();
        for (int i = 0; i < jsonDatas.length; i++) {
            if (jsonDatas[i].contains("\"Pod\"")) {
                pods.add("\\{\"apiVersion\"" + jsonDatas[i].substring(0, jsonDatas[i].length() - 1));
            }
            if (jsonDatas[i].contains("\"Service\"")) {
                svcs.add("\\{\"apiVersion\"" + jsonDatas[i].substring(0, jsonDatas[i].length() - 1));
            }
        }
        //judge mep-agent
        List<Capability> list = capabilityMapper.selectByProjectId(applicationId);
        ContainerAppPodInfo podData = handlePodData(pods, list);
        String svcData = handleSvcData(svcs);
        List<HelmChart> helmChartList = helmChartMapper.getHelmChartsByAppId(applicationId);
        if (!CollectionUtils.isEmpty(helmChartList)) {
            for (HelmChart helmChart : helmChartList) {
                helmChartMapper.deleteFileAndImage(helmChart.getId(), helmChart.getHelmChartFileId(), applicationId);
            }
        }
        String fileId = ContainerAppHelmChartUtil.writeContentToFile(podData.getPodInfo() + svcData);
        String fileName = saveFileRecord(fileId);
        saveHelmChart(fileId, fileName, applicationId);

        //save image
        ContainerAppImageInfo imageInfo = new ContainerAppImageInfo();
        imageInfo.setId(UUID.randomUUID().toString());
        imageInfo.setApplicationId(applicationId);
        imageInfo.setImageInfo(podData.getPodImageInfo());
        imageInfo.setHelmChartFileId(fileId);
        int resImage = appImageInfoMapper.saveImageInfo(imageInfo);
        if (resImage <= 0) {
            throw new DataBaseException("save image failed!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return helmChartMapper.getHelmChartByFileId(fileId);
    }

    private void saveHelmChart(String fileId, String fileName, String applicationId) {
        HelmChart helmChart = new HelmChart();
        helmChart.setId(UUID.randomUUID().toString());
        helmChart.setName(fileName);
        helmChart.setHelmChartFileId(fileId);
        int ret = helmChartMapper.createHelmChart(applicationId, helmChart);
        if (ret < 1) {
            throw new DataBaseException("save file record to db failed!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
    }

    private String saveFileRecord(String fileId) {
        UploadedFile result = new UploadedFile();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "deploy" + sdf.format(new Date()) + ".yaml";
        result.setFileName(fileName);
        result.setFileId(fileId);
        result.setUserId(AccessUserUtil.getUserId());
        result.setUploadDate(new Date());
        result.setTemp(false);
        result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
        int ret = uploadedFileMapper.saveFile(result);
        if (ret < 1) {
            throw new DataBaseException("save file record to db failed!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return fileName;
    }

    private ContainerAppPodInfo handlePodData(List<String> pods, List<Capability> list) {
        //save service
        StringBuilder podStr = new StringBuilder();
        List<String> podImageStr = new ArrayList<>();
        for (int i = 0; i < pods.size(); i++) {
            //get podName and image
            String jsonPod = pods.get(i).substring(1);
            DeployYaml deployYaml = new Gson().fromJson(jsonPod, DeployYaml.class);
            if (!CollectionUtils.isEmpty(list) && i == 0) {
                jsonPod = addMepAgent(deployYaml);
            }
            Containers[] containers = deployYaml.getSpec().getContainers();
            String[] images = new String[containers.length];
            for (int j = 0; j < containers.length; j++) {
                if (containers[j] != null) {
                    images[j] = containers[j].getImage();
                }
            }
            String imageStr = ArrayUtils.toString(images);
            imageStr = imageStr.substring(1,imageStr.length()-1);
            podImageStr.add(imageStr);
            //convert to yaml and save it
            JsonNode jsonNodeTree = null;
            String jsonAsYaml = null;
            try {
                jsonNodeTree = new ObjectMapper().readTree(jsonPod);
                jsonAsYaml = new YAMLMapper().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
                    .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true).writeValueAsString(jsonNodeTree);
            } catch (JsonProcessingException e) {
                LOGGER.error("convert pod data to yaml and save it as String {}", e.getMessage());
                return null;
            }
            podStr.append(jsonAsYaml);
        }
        LOGGER.warn("podStr:{}", podStr);
        LOGGER.warn("podImages:{}", podImageStr);
        ContainerAppPodInfo containerAppPodInfo = new ContainerAppPodInfo();
        containerAppPodInfo.setPodInfo(podStr.toString());
        containerAppPodInfo.setPodImageInfo(podImageStr.toString());
        return containerAppPodInfo;
    }

    private String handleSvcData(List<String> svcs) {
        StringBuilder svcStr = new StringBuilder();
        for (String svc : svcs) {
            //convert svc to yaml and save it
            JsonNode jsonNodeTree = null;
            String jsonAsYaml = null;
            try {
                jsonNodeTree = new ObjectMapper().readTree(svc.substring(1));
                jsonAsYaml = new YAMLMapper().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
                    .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true).writeValueAsString(jsonNodeTree);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            svcStr.append(jsonAsYaml);
        }
        LOGGER.warn("svcStr:{}", svcStr);
        return svcStr.toString();
    }

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
