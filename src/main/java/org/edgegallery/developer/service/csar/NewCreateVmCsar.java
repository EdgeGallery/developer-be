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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.vm.VmUserData;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.util.CompressFileUtils;
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
    public File create(String projectPath, VmPackageConfig config, ApplicationProject project)
        throws IOException, DomainException {
        File projectDir = new File(projectPath);

        String vmName = config.getVmName();
        // copy template files to the new project path
        File csar = DeveloperFileUtils
            .copyDirAndReName(new File(WORKSPACE_CSAR_PATH), projectDir, config.getAppInstanceId());
        // modify the mf files and fill in the data
        boolean modifyMfFileResult = modifyMfFile(csar.getCanonicalPath(), project, vmName);
        if (!modifyMfFileResult) {
            LOGGER.error("modify mf file is error");
        }

        // modify the csar  APPD/TOSCA_VNFD.meta file
        boolean modifyMetaFileResult = modifyMetaFile(csar.getCanonicalPath(), vmName);
        if (!modifyMetaFileResult) {
            LOGGER.error("modify meta file is error");
        }

        boolean modifyAppdFileResult = modifyAppdFile(csar.getCanonicalPath(), config, project);
        if (!modifyAppdFileResult) {
            LOGGER.error("modify meta file is error");
        }

        //update SwImageDesc.json , get image url
        modifyImageFile(config, csar.getCanonicalPath(), project);

        return csar;
    }

    private void modifyImageFile(VmPackageConfig config, String canonicalPath, ApplicationProject project) {
        String url = config.getVmSystem().getSystemPath();
        String imageId = url.substring(url.length() - 52, url.length() - 16);
        ImageDesc imageDesc = new ImageDesc();
        imageDesc.setId(imageId);
        imageDesc.setName(config.getVmSystem().getSystemName());
        imageDesc.setVersion(config.getVmSystem().getVersion());
        imageDesc.setChecksum(config.getVmSystem().getFileMd5());
        imageDesc.setDiskFormat(config.getVmSystem().getSystemFormat());
        imageDesc.setMinDisk(config.getVmSystem().getSystemDisk());
        if (project.getPlatform().get(0).equals("X86")) {
            imageDesc.setArchitecture("x86_64");
        } else {
            imageDesc.setArchitecture("aarch64");
        }
        imageDesc.setSwImage(config.getVmSystem().getSystemPath());
        imageDesc.setOperatingSystem(config.getVmSystem().getOperateSystem());
        List<ImageDesc> imageDescs = new ArrayList<>();
        imageDescs.add(imageDesc);
        // write data into imageJson file
        Gson gson = new Gson();
        File imageJson = new File(canonicalPath + IMAGE_BASE_PATH);
        writeFile(imageJson, gson.toJson(imageDescs));
    }

    private boolean modifyAppdFile(String canonicalPath, VmPackageConfig config, ApplicationProject project) {
        String templateName = config.getVmName() + "_" + config.getVmSystem().getOperateSystem();
        String imageName = config.getVmSystem().getSystemName();
        try {
            File resourceFile = new File(canonicalPath + TEMPLATE_CSAR_BASE_PATH);

            FileUtils.writeStringToFile(resourceFile,
                FileUtils.readFileToString(resourceFile, StandardCharsets.UTF_8).replace("<vnfd_id>", templateName)
                    .replace("<vnfd_name>", templateName).replace("<app_provider>", project.getProvider())
                    .replace("<app_name>", config.getVmName()).replace("<product_version>", project.getVersion())
                    .replace("<virtual_mem_size>", Integer.toString(config.getVmRegulation().getMemory() * 1024))
                    .replace("<num_virtual_cpu>", Integer.toString(config.getVmRegulation().getCpu()))
                    .replace("<cpu_architecture>", config.getVmRegulation().getArchitecture())
                    .replace("<size_of_storage>", Integer.toString(config.getVmRegulation().getDataDisk()))
                    .replace("<sw_image_data>", imageName).replace("<properties_name>", templateName),
                StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            LOGGER.error("modify APPD file is error");
            return false;
        }

        String mainServiceTemplatePath = canonicalPath + TEMPLATE_CSAR_BASE_PATH;
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
                //replace contents: forcontents: |
                List<String> contentsList = replaceContents(templateFile, list);
                //replace params:null params:
                replaceParams(templateFile, contentsList);
            }
        }
        //if temp false ,delete user_data
        if (vmUserData != null && (!vmUserData.isTemp() || StringUtils.isEmpty(vmUserData.getContents()) || StringUtils
            .isEmpty(vmUserData.getParams()))) {
            List<String> list = deleteUserData(templateFile);
            if (!CollectionUtils.isEmpty(list)) {
                LOGGER.warn("deleted list {}", list);
            }
        }
        File templateFileModify = new File(mainServiceTemplatePath);
        boolean isRename = templateFileModify
            .renameTo(new File(canonicalPath + "/APPD/Definition/" + config.getVmName() + ".yaml"));
        if (!isRename) {
            LOGGER.error("rename {}.yaml failed!", config.getVmName());
            return false;
        }
        // compress to zip
        String chartsDir = canonicalPath + File.separator + "APPD";
        CompressFileUtils.fileToZip(chartsDir, config.getVmName());

        return true;

    }

    private boolean modifyMetaFile(String canonicalPath, String vmName) {
        // modify the csar  TOSCA_VNFD.meta  file
        try {
            File vnfValue = new File(canonicalPath + TEMPLATE_TOSCA_VNFD__PATH);
            FileUtils.writeStringToFile(vnfValue,
                FileUtils.readFileToString(vnfValue, StandardCharsets.UTF_8).replace("{VNFD}", vmName + ".yaml"),
                StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            LOGGER.error("modify TOSCA_VNFD.meta file is error");
            return false;
        }
        // modify the csar  TOSCA-Metadata/TOSCA.meta file
        try {
            File toscaValue = new File(canonicalPath + TEMPLATE_TOSCA_METADATA_PATH);

            FileUtils.writeStringToFile(toscaValue,
                FileUtils.readFileToString(toscaValue, StandardCharsets.UTF_8).replace("{appdFile}", vmName),
                StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            LOGGER.error("modify TOSCA.meta file is error");
            return false;
        }
        return true;
    }

    private boolean modifyMfFile(String packagePath, ApplicationProject project, String vmName) {
        String deployType = (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) ? "container" : "vm";
        String projectName = project.getName();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStamp = time.format(new Date());
        try {
            File csarValue = new File(packagePath + simpleFiles);

            FileUtils.writeStringToFile(csarValue,
                FileUtils.readFileToString(csarValue, StandardCharsets.UTF_8).replace("{name}", projectName)
                    .replace("{time}", timeStamp).replace("{description}", project.getDescription())
                    .replace("{class}", deployType).replace("{provider}", project.getProvider())
                    .replace("{version}", project.getVersion()).replace("{appd-name}", vmName), StandardCharsets.UTF_8,
                false);
            boolean isSuccess = csarValue.renameTo(new File(packagePath + "/" + vmName + ".mf"));
            if (!isSuccess) {
                LOGGER.error("rename mf file failed!");
                return false;
            }

        } catch (IOException e) {
            LOGGER.error("modify mf file is error");
            return false;
        }
        return true;
    }

    private List<String> replaceContents(File templateFile, List<String> list) {
        //ObtaincontentsLocation index
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
        //ObtaincontentsLocation index
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
        //yamlRead aslist
        List<String> list = readFileByLine(templateFile);
        //ObtainflavorLocation index
        String flavor = "";
        for (String str : list) {
            if (str.contains("flavor_extra_specs:")) {
                flavor = str;
            }
        }
        int flavorIndex = list.indexOf(flavor);
        //flavor_extra_specsInsert content after position
        String unescapeContents = StringEscapeUtils.unescapeJava(flavorContent);
        List<String> contentsList = readStringToList(unescapeContents);
        list.addAll(flavorIndex + 1, contentsList);
        //RewritelistWriteyaml
        writeListToFile(list, templateFile);
    }

    private List<String> deleteUserData(File templateFile) {
        //yamlRead aslist
        List<String> list = readFileByLine(templateFile);
        //Obtain user_data Location index
        String userData = "";
        for (String str : list) {
            if (str.contains("user_data:")) {
                userData = str;
            }
        }
        int dataIndex = list.indexOf(userData);
        list.remove(dataIndex);
        list.remove(dataIndex);
        list.remove(dataIndex);
        writeListToFile(list, templateFile);
        return list;
    }

    private List<String> writeUserDataToYaml(File templateFile, VmPackageConfig config) {
        //yamlRead aslist
        List<String> list = readFileByLine(templateFile);
        //ObtaincontentsLocation index
        String contents = "";
        for (String str : list) {
            if (str.contains("contents:")) {
                contents = str;
            }
        }
        int contentIndex = list.indexOf(contents);
        //contentsInsert content after position
        VmUserData vmUserData = config.getVmUserData();
        if (StringUtils.isEmpty(vmUserData.getContents())) {
            LOGGER.warn("vm user data don't have contents configuration!");
            return Collections.emptyList();
        }
        String unescapeContents = StringEscapeUtils.unescapeJava(vmUserData.getContents());
        List<String> contentsList = readStringToList(unescapeContents);
        list.addAll(contentIndex + 1, contentsList);
        //ObtainparamsLocation index
        String params = "";
        for (String str : list) {
            if (str.contains("params:")) {
                params = str;
            }
        }
        int paramsIndex = list.indexOf(params);
        //paramsInsert content after position
        if (StringUtils.isEmpty(vmUserData.getParams())) {
            LOGGER.warn("vm user data don't have params configuration!");
            return Collections.emptyList();
        }
        String unescapeParams = StringEscapeUtils.unescapeJava(vmUserData.getParams());
        List<String> paramsList = readStringToList(unescapeParams);
        list.addAll(paramsIndex + 1, paramsList);
        //RewritelistWriteyaml
        writeListToFile(list, templateFile);
        return list;

    }

    private static void writeListToFile(List<String> strings, File yaml) {
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(yaml), StandardCharsets.UTF_8))) {
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

}
