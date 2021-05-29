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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        String templateName = projectName + "_" + project.getPlatform().get(0) + "_" + config.getVmSystem()
            .getOperateSystem();

        // modify the csar files and fill in the data
        try {
            File csarValue = new File(csar.getCanonicalPath() + simpleFiles);

            FileUtils.writeStringToFile(csarValue,
                FileUtils.readFileToString(csarValue, StandardCharsets.UTF_8).replace("{name}", projectName)
                    .replace("{time}", timeStamp).replace("{description}", project.getDescription())
                    .replace("{ChartName}", chartName).replace("{class}", deployType)
                    .replace("{app_type}", templateName).replace("{appd-name}", projectName), StandardCharsets.UTF_8,
                false);
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

            FileUtils.writeStringToFile(toscaValue,
                FileUtils.readFileToString(toscaValue, StandardCharsets.UTF_8).replace("{appdFile}", projectName)
                    .replace("{imageFile}", config.getVmSystem().getSystemName()), StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new IOException("replace file exception");
        }
        //update vm config data

        String imageName = config.getVmSystem().getSystemName();

        try {
            File resourceFile = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH);

            FileUtils.writeStringToFile(resourceFile,
                FileUtils.readFileToString(resourceFile, StandardCharsets.UTF_8).replace("<vnfd_id>", templateName)
                    .replace("<vnfd_name>", templateName).replace("<app_provider>", project.getProvider())
                    .replace("<app_name>", projectName).replace("<product_version>", project.getVersion())
                    .replace("<virtual_mem_size>", Integer.toString(config.getVmRegulation().getMemory() * 1024))
                    .replace("<num_virtual_cpu>", Integer.toString(config.getVmRegulation().getCpu()))
                    .replace("<cpu_architecture>", config.getVmRegulation().getArchitecture())
                    .replace("<size_of_storage>", Integer.toString(config.getVmRegulation().getDataDisk()))
                    .replace("<sw_image_data>", imageName), StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new IOException("replace file exception");
        }

        String mainServiceTemplatePath = csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH;
        File templateFile = new File(mainServiceTemplatePath);

        // write user data
        VmUserData vmUserData = config.getVmUserData();
        //write flavor_extra_specs
        if (vmUserData != null && !StringUtils.isEmpty(vmUserData.getFlavorExtraSpecs())) {
            String flavors = vmUserData.getFlavorExtraSpecs();
            writeFlavorToYaml(templateFile, flavors);
        }
        //write contents/ params
        if (vmUserData != null && vmUserData.isTemp()) {
            List<String> list = writeUserDataToYaml(templateFile, config);
            if (!CollectionUtils.isEmpty(list)) {
                //replace contents: 为contents: |
                List<String> contentsList = replaceContents(templateFile, list);
                //replace params:null params:
                replaceParams(templateFile, contentsList);
            }
        }
        File templateFileModify = new File(mainServiceTemplatePath);
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
        String url = config.getVmSystem().getSystemPath();
        String imageId = url.substring(url.length() - 32);
        ImageDesc imageDesc = new ImageDesc();
        imageDesc.setId(imageId);
        imageDesc.setName(config.getVmSystem().getSystemName());
        imageDesc.setVersion(project.getVersion());
        imageDesc.setChecksum("2");
        imageDesc.setContainerFormat("bare");
        imageDesc.setDiskFormat("raw");
        imageDesc.setMinDisk(3);
        imageDesc.setMinRam(6);
        imageDesc.setArchitecture(project.getPlatform().get(0));
        imageDesc.setSize(688390);
        imageDesc.setSwImage(config.getVmSystem().getSystemPath());
        imageDesc.setHwScsiModel("virtio-scsi");
        imageDesc.setHwDiskBus("scsi");
        imageDesc.setOperatingSystem(config.getVmSystem().getOperateSystem());
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

    private void writeFlavorToYaml(File templateFile, String flavorContent) {
        //yaml读取成list
        List<String> list = readFileByLine(templateFile);
        //获取flavor位置的索引
        String flavor = "";
        for (String str : list) {
            if (str.contains("flavor_extra_specs:")) {
                flavor = str;
            }
        }
        int flavorIndex = list.indexOf(flavor);
        //flavor_extra_specs位置之后插入内容
        String unescapeContents = StringEscapeUtils.unescapeJava(flavorContent);
        List<String> contentsList = readStringToList(unescapeContents);
        list.addAll(flavorIndex + 1, contentsList);
        //重写把list写入yaml
        writeListToFile(list, templateFile);
    }

    private List<String> writeUserDataToYaml(File templateFile, VmPackageConfig config) {
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
            return Collections.emptyList();
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
            return Collections.emptyList();
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
            return Collections.emptyList();
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
