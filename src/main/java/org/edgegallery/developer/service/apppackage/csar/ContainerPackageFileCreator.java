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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.model.application.Script;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.service.apppackage.converter.AppDefinitionConverter;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;

public class ContainerPackageFileCreator extends PackageFileCreator {

    private static final String TEMPLATE_PACKAGE_HELM_CHART_PATH = "/Artifacts/Deployment/Charts/";

    private static final String TEMPLATE_PACKAGE_SCRIPTS = "/Artifacts/Deployment/Scripts/";

    private static final String TEMPLATE_APPD = "APPD/";

    private static final String TEMPLATE_PATH = "temp";

    private ContainerApplication application;

    private String packageId;

    private String getHelmChartName() {
        return null;
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

    private File generateHelmChart() {

        return new File(getPackageBasePath());

    }

    public void generateImageDesFile() {

    }

    public File generatePackageFile() {
        return new File(getPackageBasePath());
    }

    private String getPackageBasePath() {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + application.getId()
            + File.separator;
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
        try {
            String helmChartPath = tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH + getHelmChartName();
            File chartFileDir = new File(helmChartPath);
            if (!chartFileDir.exists() || !chartFileDir.isDirectory()) {
                LOGGER.error("helm chart file does not exist, file name is:{}", getHelmChartName());
                return false;
            }
            File tgz = CompressFileUtils
                .compressToTgzAndDeleteSrc(helmChartPath, tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH,
                    getHelmChartName());
            if (!tgz.exists()) {
                LOGGER.error("Create tgz exception, file name is:{}", getHelmChartName());
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("helm chart file  compress fail, file name is:{}", getHelmChartName());
            return false;
        }
        return true;
    }
}
