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

package org.edgegallery.developer.service.csar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.vm.VmFlavor;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.vm.VmUserData;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class NewCreateVmCsar {

    private static final String simpleFiles = "/app-name.mf";

    private static final String WORKSPACE_CSAR_PATH = "./configs/vm_csar";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/APPD/Definition/app-name.yaml";

    private static final String TEMPLATE_TOSCA_VNFD__PATH = "/APPD/TOSCA_VNFD.meta";

    private static final String TEMPLATE_TOSCA_METADATA_PATH = "/TOSCA-Metadata/TOSCA.meta";

    private static final String IMAGE_BASE_PATH = "/Image/SwImageDesc.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(NewCreateVmCsar.class);

    /**
     * create vm csar.
     *
     * @param projectPath path of project
     * @param config vm create config of project
     * @param project project self
     * @return package gz
     */
    public File create(String projectPath, VmPackageConfig config, ApplicationProject project, VmFlavor flavor,
        List<VmNetwork> vmNetworks) throws IOException, DomainException {
        File projectDir = new File(projectPath);

        String deployType = (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) ? "container" : "vm";
        String projectName = project.getName();
        String chartName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();

        // copy template files to the new project path
        File csar = DeveloperFileUtils
            .copyDirAndReName(new File(WORKSPACE_CSAR_PATH), projectDir, config.getAppInstanceId());

        // get data to Map<String, String>
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStamp = time.format(new Date());

        // modify the csar files and fill in the data
        try {
            File csarValue = new File(csar.getCanonicalPath() + simpleFiles);

            FileUtils.writeStringToFile(csarValue,
                FileUtils.readFileToString(csarValue, StandardCharsets.UTF_8).replace("{name}", projectName)
                    .replace("{provider}", project.getProvider()).replace("{version}", project.getVersion())
                    .replace("{time}", timeStamp).replace("{description}", project.getDescription())
                    .replace("{ChartName}", chartName).replace("{class}", deployType)
                    .replace("{appd-name}", projectName),
                StandardCharsets.UTF_8, false);
            boolean isSuccess = csarValue.renameTo(new File(csar.getCanonicalPath() + "/" + projectName + ".mf"));
            if (!isSuccess) {
                LOGGER.error("rename mf file failed!");
                return null;
            }

        } catch (IOException e) {
            throw new IOException("replace file exception");
        }

        // modify the csar  APPD/TOSCA_VNFD.meta file
        try {
            File vnfValue = new File(csar.getCanonicalPath() + TEMPLATE_TOSCA_VNFD__PATH);

            FileUtils.writeStringToFile(vnfValue,
                FileUtils.readFileToString(vnfValue, StandardCharsets.UTF_8).replace("{VNFD}", projectName + ".yaml"),
                StandardCharsets.UTF_8, false);

        } catch (IOException e) {
            throw new IOException("replace file exception");
        }

        // modify the csar  TOSCA-Metadata/TOSCA.meta file
        try {
            File toscaValue = new File(csar.getCanonicalPath() + TEMPLATE_TOSCA_METADATA_PATH);

            FileUtils.writeStringToFile(toscaValue, FileUtils.readFileToString(toscaValue, StandardCharsets.UTF_8)
                .replace("{appdFile}", projectName), StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new IOException("replace file exception");
        }
        //update vm config data

        String mainServiceTemplatePath = csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH;
        File templateFile = new File(mainServiceTemplatePath);
        String yamlContent = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
        yamlContent = yamlContent.replaceAll("\t", "");
        Yaml yaml = new Yaml(new SafeConstructor());
        Map<String, Object> loaded;
        try {
            loaded = yaml.load(yamlContent);
        } catch (DomainException e) {
            LOGGER.error("Yaml deserialization failed {}", e.getMessage());
            throw new DomainException("Yaml deserialization failed");
        }

        // modify vnf info
        LinkedHashMap<String, Object> vmName = getObjectFromMap(loaded, "metadata");
        vmName.put("template_name", projectName);
        vmName.put("vnfd_name", projectName);
        vmName.put("vnfd_id", projectName);
        vmName.put("vnfd_version", project.getVersion());
        // config vm name
        LinkedHashMap<String, Object> virtualName = getObjectFromMap(loaded, "topology_template", "node_templates",
            "EMS_VDU1", "properties");
        virtualName.put("name", config.getVmName());
        // config vm memory
        LinkedHashMap<String, Object> virtualMemory = getObjectFromMap(loaded, "topology_template", "node_templates",
            "EMS_VDU1", "capabilities", "virtual_compute", "properties", "virtual_memory");
        virtualMemory.put("virtual_mem_size", config.getVmRegulation().getMemory() * 1024);
        // config vm cpu
        LinkedHashMap<String, Object> virtualCpu = getObjectFromMap(loaded, "topology_template", "node_templates",
            "EMS_VDU1", "capabilities", "virtual_compute", "properties", "virtual_cpu");
        virtualCpu.put("num_virtual_cpu", config.getVmRegulation().getCpu());
        // config vm cpu_architecture
        virtualCpu.put("cpu_architecture", config.getVmRegulation().getArchitecture());
        // config vm data storage
        LinkedHashMap<String, Object> virtualStorage = getObjectFromMap(loaded, "topology_template", "node_templates",
            "EMS_VDU1", "capabilities", "virtual_compute", "properties", "virtual_local_storage");
        virtualStorage.put("size_of_storage", config.getVmRegulation().getDataDisk());
        // config vm image data
        String imageData = config.getVmSystem().getOperateSystem() + "-" + config.getVmSystem().getVersion();
        LinkedHashMap<String, Object> virtualImage = getObjectFromMap(loaded, "topology_template", "node_templates",
            "EMS_VDU1", "properties", "sw_image_data");
        virtualImage.put("name", imageData);

        // config flavor
        LinkedHashMap<String, Object> virtualFlavor = getObjectFromMap(loaded, "topology_template", "node_templates",
            "EMS_VDU1", "properties", "vdu_profile", "flavor_extra_specs");
        virtualFlavor.remove("mgmt_egarm", "true");
        virtualFlavor.put(flavor.getFlavor(), "true");

        // config flavor
        LinkedHashMap<String, Object> virtualConstraints = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1", "properties");
        virtualConstraints.put("nfvi_constraints", flavor.getConstraints());

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.writeValue(templateFile, loaded);
        // write user data
        List<String> list = writeUserDataToYaml(templateFile, config);
        //replace contents: 为contents: |
        List<String> contentsList = replaceContents(templateFile, list);
        //replace params:null params:
        replaceParams(templateFile, contentsList);
        // delete ""
        File templateFileModify = new File(mainServiceTemplatePath);
        String yamlContents = FileUtils.readFileToString(templateFileModify, StandardCharsets.UTF_8);
        yamlContents = yamlContents.replaceAll("\"", "");
        writeFile(templateFileModify, yamlContents);
        boolean isRename = templateFileModify
            .renameTo(new File(csar.getCanonicalPath() + "/APPD/Definition/" + projectName + ".yaml"));
        if (!isRename) {
            LOGGER.error("rename {}.yaml failed!", projectName);
            return null;
        }
        // compress to zip
        String chartsDir = csar.getParent() + File.separator + config.getAppInstanceId() + File.separator + "APPD";
        if (!StringUtils.isEmpty(chartsDir)) {
            File dir = new File(chartsDir);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    List<File> subFiles = Arrays.asList(files);
                    if (!CollectionUtils.isEmpty(subFiles)) {
                        CompressFileUtilsJava
                            .zipFiles(subFiles, new File(chartsDir + File.separator + projectName + ".zip"));
                        for (File subFile : subFiles) {
                            FileUtils.deleteQuietly(subFile);
                        }
                    }
                }
            }

        }
        //update SwImageDesc.json , get image url
        ImageDesc imageDesc = new ImageDesc();
        imageDesc.setId(UUID.randomUUID().toString());
        imageDesc.setName(imageData);
        imageDesc.setVersion(project.getVersion());
        imageDesc.setChecksum("2");
        imageDesc.setContainerFormat("bare");
        imageDesc.setDiskFormat("raw");
        imageDesc.setMinDisk(3);
        imageDesc.setMinRam(6);
        imageDesc.setArchitecture(project.getPlatform().get(0));
        imageDesc.setSize(688390);
        imageDesc.setSwImage("Image/" + imageData);
        imageDesc.setHwScsiModel("virtio-scsi");
        imageDesc.setHwDiskBus("scsi");
        imageDesc.setOperatingSystem("linux");
        imageDesc.setSupportedVirtualisationEnvironment("linux");
        List<ImageDesc> imageDescs = new ArrayList<>();
        imageDescs.add(imageDesc);
        // write data into imageJson file
        Gson gson = new Gson();
        File imageJson = new File(csar.getCanonicalPath() + IMAGE_BASE_PATH);
        writeFile(imageJson, gson.toJson(imageDescs));

        return csar;
    }

    private List<String> replaceContents(File templateFile, List<String> list) {
        //获取contents位置的索引
        String contents = "";
        for (String str : list) {
            if (str.contains("contents:")) {
                contents = str;
            }
        }
        int contentIndex = list.indexOf(contents);
        list.set(contentIndex, "            contents: |\r\n");
        writeListToFile(list, templateFile);
        return list;
    }

    private void replaceParams(File templateFile, List<String> list) {
        //获取contents位置的索引
        String params = "";
        for (String str : list) {
            if (str.contains("params:")) {
                params = str;
            }
        }
        int contentIndex = list.indexOf(params);
        list.set(contentIndex, "            params:\r\n");
        writeListToFile(list, templateFile);
    }

    private List<String> writeUserDataToYaml(File templateFile, VmPackageConfig config) {
        if (config.getVmUserData() == null) {
            LOGGER.error("no vm user data!");
            return null;
        }
        //yaml读取成list
        List<String> list = readFileByLine(templateFile);
        //获取contents位置的索引
        String contents = "";
        for (String str : list) {
            if (str.contains("contents:")) {
                contents = str;
            }
        }
        int contentIndex = list.indexOf(contents);
        //contents位置之后插入内容
        VmUserData vmUserData = config.getVmUserData();
        if (StringUtils.isEmpty(vmUserData.getContents())) {
            LOGGER.warn("vm user data don't have contents configuration!");
            return null;
        }
        String unescapeContents = StringEscapeUtils.unescapeJava(vmUserData.getContents());
        List<String> contentsList = readStringToList(unescapeContents);
        list.addAll(contentIndex + 1, contentsList);
        //获取params位置的索引
        String params = "";
        for (String str : list) {
            if (str.contains("params:")) {
                params = str;
            }
        }
        int paramsIndex = list.indexOf(params);
        //params位置之后插入内容
        if (StringUtils.isEmpty(vmUserData.getParams())) {
            LOGGER.warn("vm user data don't have params configuration!");
            return null;
        }
        String unescapeParams = StringEscapeUtils.unescapeJava(vmUserData.getParams());
        List<String> paramsList = readStringToList(unescapeParams);
        list.addAll(paramsIndex + 1, paramsList);
        //重写把list写入yaml
        writeListToFile(list, templateFile);
        return list;

    }

    private static void writeListToFile(List<String> strings, File yaml) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(yaml))) {
            for (String l : strings) {
                writer.write(l);
            }
        } catch (IOException e) {
            LOGGER.error("write file content list to file failed {}", e.getMessage());
        }
    }

    private static List<String> readFileByLine(File fin) {
        String line;
        List<String> sb = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(fin);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                sb.add(line + "\r\n");
            }
        } catch (IOException e) {
            return null;
        }
        return sb;
    }

    private static List<String> readStringToList(String contents) {
        String line;
        List<String> sb = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                sb.add("              " + line + "\r\n");
            }
        } catch (IOException e) {
            return null;
        }
        return sb;
    }

    private void writeFile(File file, String content) {
        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

    /**
     * getObjectFromMap.
     */
    public static LinkedHashMap<String, Object> getObjectFromMap(Map<String, Object> loaded, String... keys) {
        LinkedHashMap<String, Object> result = null;
        for (String key : keys) {
            result = (LinkedHashMap<String, Object>) loaded.get(key);
            if (result != null) {
                loaded = result;
            }
        }
        return result;
    }

}
