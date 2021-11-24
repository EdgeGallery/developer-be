/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.service.apppackage.csar;

import com.google.gson.Gson;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.model.application.Script;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.apppackage.converter.AppDefinitionConverter;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.edgegallery.developer.util.helmcharts.k8sObject.EnumKubernetesObject;
import org.edgegallery.developer.util.helmcharts.k8sObject.IContainerImage;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class ContainerPackageFileCreator extends PackageFileCreator {

    private ContainerAppHelmChartService helmChartService = (ContainerAppHelmChartService) SpringContextUtil
        .getBean(ContainerAppHelmChartService.class);

    private UploadFileService uploadFileService = (UploadFileService) SpringContextUtil
        .getBean(UploadFileService.class);

    private static final String TEMPLATE_PACKAGE_HELM_CHART_PATH = "/Artifacts/Deployment/Charts/";

    private static final String TEMPLATE_PACKAGE_SCRIPTS = "/Artifacts/Deployment/Scripts/";

    private static final String APPD_IMAGE_DES_PATH = "/Image/SwImageDesc.json";

    private static final String TEMPLATE_APPD = "APPD/";

    private static final String TEMPLATE_PATH = "temp";

    private ContainerApplication application;

    private String packageId;

    private List<String> getHelmChartNameList() {
        List<String> chartNameList = new ArrayList<>();
        List<HelmChart> chartList = helmChartService.getHelmChartList(application.getId());
        for (HelmChart chart : chartList) {
            UploadFile uploadFile = uploadFileService.getFile(chart.getHelmChartFileId());
            String decompressFolderName = uploadFile.getFileName()
                .substring(0, uploadFile.getFileName().lastIndexOf("."));
            chartNameList.add(decompressFolderName);
        }
        return chartNameList;
    }

    public ContainerPackageFileCreator(ContainerApplication application, String packageId) {
        super(application, packageId);
        this.application = application;
        this.packageId = packageId;
    }

    public String generateAppPackageFile() {
        String packagePath = getPackagePath();
        if (!copyPackageTemplateFile()) {
            LOGGER.error("copy package template file fail, package dir:{}", packagePath);
            return null;
        }
        configMfFile();
        configMetaFile();
        configVnfdMeta();
        generateAPPDYaml();
        generateImageDesFile();
        generateHelmChart();
        configMdAndIcon();
        generateScript();
        String compressPath = PackageFileCompress();
        if (null == compressPath) {
            LOGGER.error("package compress fail");
            return null;
        }
        return compressPath;
    }

    private boolean generateAPPDYaml() {
        String appdFilePath = getAppdFilePath();
        AppDefinitionConverter converter = new AppDefinitionConverter();
        AppDefinition appDefinition = converter.convertApplication2Appd(appdFilePath, this.application);
        return converter.saveAppdYaml(appdFilePath, appDefinition);
    }

    private void generateHelmChart() {

        //Find the helm chart file first!
        List<HelmChart> chartList = helmChartService.getHelmChartList(application.getId());
        for (HelmChart chart : chartList) {
            UploadFile uploadFile = uploadFileService.getFile(chart.getHelmChartFileId());
            File chartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
            //decompress
            String outputDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath()
                + uploadFile.getFileId() + File.separator;
            try {
                 //decompress
                CompressFileUtils.decompress(chartFile.getCanonicalPath(), outputDir);
                //Gets the name of the unzipped folder
                String decompressFolderName = uploadFile.getFileName()
                    .substring(0, uploadFile.getFileName().lastIndexOf("."));
                // herm chart file unzipped path
                String helmChartPath = outputDir + decompressFolderName + File.separator;
                File srcDir = new File(helmChartPath);
                File destDir = new File(getPackagePath() + TEMPLATE_PACKAGE_HELM_CHART_PATH);
                FileUtils.copyDirectoryToDirectory(srcDir, destDir);
            } catch (IOException e) {
                LOGGER.error("copy unzipped helm chart Folder to Charts folder occur {}", e.getMessage());
                break;
            }
        }

    }

    public void generateImageDesFile() {
        List<String> imageList = getImageInfo();
        if (CollectionUtils.isEmpty(imageList)) {
            LOGGER.error("yaml file is not configured with any image information");
            return;
        }
        List<ImageDesc> imageDescs = new ArrayList<>();
        for (String imageInfo : imageList) {
            ImageDesc imageDesc = new ImageDesc();
            imageDesc.setId(UUID.randomUUID().toString());
            if (imageInfo.contains("/")) {
                String[] imageInfoArr = imageInfo.split("/");
                String[] images = imageInfoArr[2].split(":");
                imageDesc.setName(images[0]);
                imageDesc.setVersion(images[1]);
            } else {
                String[] images = imageInfo.split(":");
                imageDesc.setName(images[0]);
                imageDesc.setVersion(images[1]);
            }
            imageDesc.setChecksum("2");
            imageDesc.setContainerFormat("bare");
            imageDesc.setDiskFormat("raw");
            imageDesc.setMinRam(6);
            imageDesc.setArchitecture("application.getArchitecture()");
            imageDesc.setSize(688390);
            imageDesc.setSwImage(imageInfo);
            imageDesc.setHwScsiModel("virtio-scsi");
            imageDesc.setHwDiskBus("scsi");
            imageDesc.setOperatingSystem("linux");
            imageDescs.add(imageDesc);
        }
        Gson gson = new Gson();
        File imageJson = new File(getPackagePath() + APPD_IMAGE_DES_PATH);
        writeFile(imageJson, gson.toJson(imageDescs));
    }

    private boolean generateScript() {
        String scriptsDirPath = getPackagePath() + TEMPLATE_PACKAGE_SCRIPTS;
        List<Script> scriptList = application.getScriptList();
        for (Script script : scriptList) {
            String scripPath = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + script
                .getScriptFileId();
            String artifactScriptPath = scriptsDirPath + script.getName();
            try {
                FileUtils.copyFile(new File(scripPath), new File(artifactScriptPath));
            } catch (IOException e) {
                LOGGER.error("generate script failed. {}", e);
                return false;
            }
        }
        return true;
    }

    public boolean compressDeploymentFile() {
        String tempPackagePath = getPackagePath() + TEMPLATE_PATH;
        File tempDir = new File(tempPackagePath);
        if(!tempDir.exists() || !tempDir.isDirectory()){
            LOGGER.error("temp dir {} can not found", tempPackagePath);
            return false;
        }
        //clean temp
        try {
            FileUtils.cleanDirectory(tempDir);
        } catch (IOException e) {
            LOGGER.error("clean temp dir failed!:{}", e.getMessage());
            return false;
        }
        List<String> chartNameList = getHelmChartNameList();
        for (String helmChartName : chartNameList) {
            try {
                String helmChartPath = tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH + helmChartName;
                File chartFileDir = new File(helmChartPath);
                if (!chartFileDir.exists() || !chartFileDir.isDirectory()) {
                    LOGGER.error("helm chart file does not exist, file name is:{}", helmChartName);
                    return false;
                }
                File tgz = CompressFileUtils.compressToTgzAndDeleteSrc(helmChartPath, tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH,
                    helmChartName);
                if (!tgz.exists()) {
                    LOGGER.error("Create tgz exception, file name is:{}", helmChartName);
                    return false;
                }
            } catch (IOException e) {
                LOGGER.error("helm chart file  compress fail, file name is:{}", helmChartName);
                return false;
            }
        }
        return true;
    }

    private List<String> getImageInfo() {
        List<String> allImages = new ArrayList<>(0);
        //Find the helm chart file first!
        List<HelmChart> chartList = helmChartService.getHelmChartList(application.getId());
        for (HelmChart chart : chartList) {
            UploadFile uploadFile = uploadFileService.getFile(chart.getHelmChartFileId());
            File chartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
            IContainerFileHandler containerFileHandler = LoadContainerFileFactory.createLoader(chartFile.getName());
            assert containerFileHandler != null;
            try {
                containerFileHandler.load(chartFile.getCanonicalPath());
            } catch (IOException e) {
               LOGGER.error("load helm chart File occur {}",e.getMessage());
               break;
            }
            List<HelmChartFile> k8sTemplates = containerFileHandler.getTemplatesFile();
            for (HelmChartFile k8sTemplate : k8sTemplates) {
                List<Object> k8s = containerFileHandler.getK8sTemplateObject(k8sTemplate);
                for (Object obj : k8s) {
                    IContainerImage containerImage = EnumKubernetesObject.of(obj);
                    if (obj instanceof V1Pod) {
                        List<String> podImages = containerImage.getImages();
                        allImages.addAll(podImages);
                    }
                    if (obj instanceof V1Deployment) {
                        List<String> deploymentImages = containerImage.getImages();
                        allImages.addAll(deploymentImages);
                    }
                }
            }
        }
        return allImages;
    }

}
