/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.deployyaml.PodImage;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class NewCreateCsar {

    private static final String simpleFiles = "/positioning-service.mf";

    private static final String WORKSPACE_CSAR_PATH = "./configs/new_csar";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/Artifacts/Deployment/Charts/";

    private static final String IMAGE_BASE_PATH = "Image/SwImageDesc.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(NewCreateCsar.class);

    /**
     * create csar.
     *
     * @param projectPath path of project
     * @param config test config of project
     * @param project project self
     * @return package gz
     */
    public File create(String projectPath, ProjectTestConfig config, ApplicationProject project, String chartName,
        File chart) throws IOException {
        File projectDir = new File(projectPath);

        String deployType = (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) ? "container" : "vm";
        String projectName = project.getName();

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
                    .replace("{ChartName}", chartName).replace("{type}", deployType), StandardCharsets.UTF_8, false);
            boolean isSuccess = csarValue.renameTo(new File(csar.getCanonicalPath() + "/" + chartName + ".mf"));
            if (!isSuccess) {
                LOGGER.warn("positioning-service.mf rename to project-name.mf failed!");
            }
        } catch (IOException e) {
            throw new IOException("replace file exception");
        }

        if (chart != null) {
            // move chart.tgz to Chart directory and delete apptgz dir
            File chartDir = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH);
            FileUtils.cleanDirectory(chartDir);
            FileUtils.moveFileToDirectory(chart, chartDir, true);
        } else {
            //compose apptgz to .tgz and delete apptgz dir
            File appTgz = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH + "app-tgz/");
            File appTgzNew = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH, chartName);
            if (!appTgz.renameTo(appTgzNew)) {
                throw new IOException("Rename tgz exception");
            }
            File tgz = CompressFileUtils
                .compressToTgzAndDeleteSrc(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH + chartName,
                    csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH, chartName);
            if (!tgz.exists()) {
                throw new IOException("Create tgz exception");
            }
        }

        // compress to zip
        String appdDir = csar.getParent() + File.separator + config.getAppInstanceId() + File.separator + "APPD";
        if (!StringUtils.isEmpty(appdDir)) {
            File dir = new File(appdDir);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    List<File> subFiles = Arrays.asList(files);
                    if (!CollectionUtils.isEmpty(subFiles)) {
                        CompressFileUtilsJava
                            .zipFiles(subFiles, new File(appdDir + File.separator + "MainServiceTemplate.zip"));
                        for (File subFile : subFiles) {
                            FileUtils.deleteQuietly(subFile);
                        }
                    }
                }
            }

        }
        //update SwImageDesc.json
        File imageJson = new File(csar.getCanonicalPath() + File.separator + IMAGE_BASE_PATH);
        //query saved pod data
        String projectId = project.getId();
        List<ProjectImageConfig> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(projectId)) {
            ProjectImageMapper imageMapper = (ProjectImageMapper) SpringContextUtil.getBean(ProjectImageMapper.class);
            list = imageMapper.getAllImage(projectId);
        }
        if (!CollectionUtils.isEmpty(list)) {
            ProjectImageConfig imageConfig = list.get(0);
            String containers = imageConfig.getPodContainers();
            String imageData = "";
            if (containers.contains("podName")) {
                List<PodImage> images = new Gson().fromJson(containers, new TypeToken<List<PodImage>>() { }.getType());
                imageData = getSwImageDataVisial(images, project);
            } else {
                List<String> podImages = new ArrayList<>();
                if (!containers.contains(",")) {
                    podImages.add(containers.substring(1, containers.length() - 1));
                } else {
                    String[] sa = containers.substring(1, containers.length() - 1).split(",");
                    for (String image : sa) {
                        podImages.add(image);
                    }
                }
                imageData = getSwImageData(podImages, project);
            }

            // write data into imageJson file
            writeFile(imageJson, imageData);
        }
        return csar;
    }

    private String getSwImageData(List<String> images, ApplicationProject project) {
        Gson gson = new Gson();
        List<ImageDesc> imageDescs = new ArrayList<>();
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        for (String image : images) {
            if (image.contains(".Values.imagelocation.domainname") || image.contains(".Values.imagelocation.project")) {
                String[] imager = image.trim().split("/");
                image = imageConfig.getDomainname() + "/" + imageConfig.getProject() + "/" + imager[2].trim();
            }
            ImageDesc imageDesc = new ImageDesc();
            imageDesc.setId(UUID.randomUUID().toString());
            String[] names = image.split("/");
            int len = names.length - 1;
            String[] vers = names[len].split(":");
            imageDesc.setName(vers[0]);
            imageDesc.setVersion(vers[1]);
            imageDesc.setChecksum("2");
            imageDesc.setContainerFormat("bare");
            imageDesc.setDiskFormat("raw");
            imageDesc.setMinDisk(3);
            imageDesc.setMinRam(6);
            imageDesc.setArchitecture(project.getPlatform().get(0));
            imageDesc.setSize(688390);
            imageDesc.setSwImage(image);
            imageDesc.setHwScsiModel("virtio-scsi");
            imageDesc.setHwDiskBus("scsi");
            imageDesc.setOperatingSystem("linux");
            imageDesc.setSupportedVirtualisationEnvironment("linux");
            imageDescs.add(imageDesc);
        }

        return gson.toJson(imageDescs);
    }

    private String getSwImageDataVisial(List<PodImage> images, ApplicationProject project) {
        Gson gson = new Gson();
        List<ImageDesc> imageDescs = new ArrayList<>();
        for (PodImage obj : images) {
            String[] podImages = obj.getPodImage();
            for (String pod : podImages) {
                ImageDesc imageDesc = new ImageDesc();
                imageDesc.setId(UUID.randomUUID().toString());
                if (pod.contains(".Values.imagelocation.domainname")) {
                    String[] imager = pod.split("/");
                    ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
                    pod = imageConfig.getDomainname() + "/" + imageConfig.getProject() + "/" + imager[2];
                }
                String[] vers = pod.split(":");
                imageDesc.setName(vers[0]);
                imageDesc.setVersion(vers[1]);
                imageDesc.setChecksum("2");
                imageDesc.setContainerFormat("bare");
                imageDesc.setDiskFormat("raw");
                imageDesc.setMinRam(6);
                imageDesc.setArchitecture(project.getPlatform().get(0));
                imageDesc.setSize(688390);
                imageDesc.setSwImage(pod);
                imageDesc.setHwScsiModel("virtio-scsi");
                imageDesc.setHwDiskBus("scsi");
                imageDesc.setOperatingSystem("linux");
                imageDescs.add(imageDesc);
            }
        }

        return gson.toJson(imageDescs);
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
