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

package org.edgegallery.developer.service.apppackage.csar.creater;

import com.google.gson.Gson;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.model.application.Script;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.apppackage.ImageDesc;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.apppackage.csar.appdconverter.AppDefinitionConverter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class ContainerPackageFileCreator extends PackageFileCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerPackageFileCreator.class);

    private ContainerAppHelmChartService helmChartService = (ContainerAppHelmChartService) SpringContextUtil
        .getBean(ContainerAppHelmChartService.class);

    private UploadFileService uploadFileService = (UploadFileService) SpringContextUtil
        .getBean(UploadFileService.class);

    private static final String TEMPLATE_PACKAGE_HELM_CHART_PATH = "/Artifacts/Deployment/Charts/";

    private static final String TEMPLATE_PACKAGE_SCRIPTS = "/Artifacts/Deployment/Scripts/";

    private static final String APPD_IMAGE_DES_PATH = "/Image/SwImageDesc.json";

    private static final String TEMPLATE_PATH = "temp";

    private ContainerApplication application;

    private String packageId;

    private List<HelmChart> chartList;

    private List<ImageDesc> imageDescList = new ArrayList<>();

    public ContainerPackageFileCreator(ContainerApplication application, String packageId) {
        super(application, packageId);
        this.application = application;
        this.packageId = packageId;
        init();
    }

    private void init() {
        try {
            chartList = helmChartService.getHelmChartList(application.getId());
            LOGGER.info("chartList:{}", chartList);
        } catch (Exception e) {
            LOGGER.error("get Helm chart list failed! {}", e.getMessage());
            chartList = Collections.emptyList();
        }
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
        generateHelmChart();
        generateImageDesFile();
        generateAPPDYaml();
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
        AppDefinition appDefinition = converter.convertApplication2Appd(this.application, imageDescList);
        return converter.saveAppdYaml(appdFilePath, appDefinition);
    }

    private void generateHelmChart() {
        //clean helm chart folder
        File destDir = new File(getPackagePath() + TEMPLATE_PACKAGE_HELM_CHART_PATH);
        if (destDir.exists()) {
            try {
                FileUtils.cleanDirectory(destDir);
            } catch (IOException e) {
                LOGGER.error("clean dir {} failed!:{}", TEMPLATE_PACKAGE_HELM_CHART_PATH, e.getMessage());
                return;
            }
        } else {
            boolean res = destDir.mkdirs();
            if (!res) {
                LOGGER.error("create chart dir failed!");
                return;
            }
        }
        //Find the helm chart file first!
        try {
            for (HelmChart chart : chartList) {
                UploadFile uploadFile = uploadFileService.getFile(chart.getHelmChartFileId());
                LOGGER.info("uploadFile path:{}", uploadFile.getFilePath());
                File chartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
                CompressFileUtils.decompress(chartFile.getCanonicalPath(), destDir.getCanonicalPath());
            }
        } catch (IOException e) {
            LOGGER.error("decompress helm chart file occur {}", e.getMessage());
            throw new FileOperateException("decompress helm chart file failed!",
                ResponseConsts.RET_DECOMPRESS_FILE_FAIL);
        }
    }

    public void generateImageDesFile() {
        List<String> imageList = getImageInfo();
        if (CollectionUtils.isEmpty(imageList)) {
            LOGGER.error("yaml file is not configured with any image information");
            return;
        }
        LOGGER.info("imageList:{}", imageList);
        for (String imageInfo : imageList) {
            //image info support a/b/c:d a:b b/c:d
            ImageDesc imageDesc = new ImageDesc();
            imageDesc.setId(UUID.randomUUID().toString());
            if (imageInfo.contains("/")) {
                String[] imageInfoArr = imageInfo.split("/");
                if (imageInfoArr.length == 3) {
                    String[] images = imageInfoArr[2].split(":");
                    imageDesc.setName(images[0]);
                    imageDesc.setVersion(images[1]);
                } else if (imageInfoArr.length == 2) {
                    String[] images = imageInfoArr[1].split(":");
                    imageDesc.setName(images[0]);
                    imageDesc.setVersion(images[1]);
                } else {
                    LOGGER.error("image {} info non-standard format", imageInfo);
                    return;
                }
            } else {
                String[] images = imageInfo.split(":");
                if (images.length == 2) {
                    imageDesc.setName(images[0]);
                    imageDesc.setVersion(images[1]);
                } else {
                    LOGGER.error("image {} non-standard format", imageInfo);
                    return;
                }
            }
            imageDesc.setArchitecture(application.getArchitecture());
            imageDesc.setSwImage(imageInfo);
            imageDescList.add(imageDesc);
        }
        Gson gson = new Gson();
        File imageJson = new File(getPackagePath() + APPD_IMAGE_DES_PATH);
        writeFile(imageJson, gson.toJson(imageDescList));
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
                LOGGER.error("generate script failed. {}", e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean compressDeploymentFile() {
        String tempPackagePath = getPackagePath() + TEMPLATE_PATH;
        File tempDir = new File(tempPackagePath);
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            LOGGER.error("temp dir {} can not found", tempPackagePath);
            return false;
        }
        List<String> chartNameList = getHelmChartNameList();
        LOGGER.info("chartNameList:{}", chartNameList);
        for (String helmChartName : chartNameList) {
            try {
                String helmChartPath = tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH + helmChartName;
                File chartFileDir = new File(helmChartPath);
                if (!chartFileDir.exists() || !chartFileDir.isDirectory()) {
                    LOGGER.error("helm chart file does not exist, file name is:{}", helmChartName);
                    return false;
                }
                File tgz = CompressFileUtils
                    .compressToTgzAndDeleteSrc(helmChartPath, tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH,
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

    private List<String> getHelmChartNameList() {
        List<String> chartNameList = new ArrayList<>();
        for (HelmChart chart : chartList) {
            UploadFile uploadFile = uploadFileService.getFile(chart.getHelmChartFileId());
            String decompressFolderName = uploadFile.getFileName()
                .substring(0, uploadFile.getFileName().lastIndexOf("."));
            chartNameList.add(decompressFolderName);
        }
        return chartNameList;
    }

    private List<String> getImageInfo() {
        List<String> allImages = new ArrayList<>(0);
        //Find the helm chart file first!
        try {
            for (HelmChart chart : chartList) {
                UploadFile uploadFile = uploadFileService.getFile(chart.getHelmChartFileId());
                File chartFile = new File(InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath());
                IContainerFileHandler containerFileHandler = LoadContainerFileFactory.createLoader(chartFile.getName());
                assert containerFileHandler != null;
                containerFileHandler.load(chartFile.getCanonicalPath());
                List<HelmChartFile> k8sTemplates = containerFileHandler.getTemplatesFile();
                for (HelmChartFile k8sTemplate : k8sTemplates) {
                    List<Object> k8sList = containerFileHandler.getK8sTemplateObject(k8sTemplate);
                    for (Object obj : k8sList) {
                        if (obj instanceof V1Pod || obj instanceof V1Deployment) {
                            IContainerImage containerImage = EnumKubernetesObject.of(obj);
                            List<String> podImages = containerImage.getImages();
                            allImages.addAll(podImages);
                        } else {
                            LOGGER.warn("{} does not support image configuration", obj.getClass());
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("get image info from service yaml occur {}", e.getMessage());
            return Collections.emptyList();
        }
        return allImages;
    }

}
