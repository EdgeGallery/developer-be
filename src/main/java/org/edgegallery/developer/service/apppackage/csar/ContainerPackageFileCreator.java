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
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;

public class ContainerPackageFileCreator extends PackageFileCreator{

    private static final String TEMPLATE_PACKAGE_HELM_CHART_PATH = "/Artifacts/Deployment/Charts/";

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

    private File generateAPPDYaml()  {

        return new File(getPackageBasePath());

    }

    private File generateHelmChart()  {

        return new File(getPackageBasePath());

    }

    private File generateImageDesFile()  {

        return new File(getPackageBasePath());

    }

    public File generatePackageFile() {
        return new File(getPackageBasePath());
    }

    private String getPackageBasePath() {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + application.getId() + File.separator;
    }

    public boolean compressDeploymentFile() {
        String tempPackagePath = getPackagePath() + TEMPLATE_PATH;
        try {
            String helmChartPath = tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH + getHelmChartName();
            File chartFileDir = new File(helmChartPath);
            if (!chartFileDir.exists() || !chartFileDir.isDirectory()) {
                LOGGER.error("helm chart file is not exited, file name is:{}", getHelmChartName());
                return false;
            }
            File tgz = CompressFileUtils
                .compressToTgzAndDeleteSrc(helmChartPath,
                    tempPackagePath + TEMPLATE_PACKAGE_HELM_CHART_PATH, getHelmChartName());
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
