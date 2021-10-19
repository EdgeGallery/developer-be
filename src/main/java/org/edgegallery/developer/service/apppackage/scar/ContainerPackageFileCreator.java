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

package org.edgegallery.developer.service.apppackage.scar;

import java.io.File;
import java.io.IOException;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;

public class ContainerPackageFileCreator {

    private ContainerApplication application;

    private String packageId;

    public ContainerPackageFileCreator(ContainerApplication application, String packageId) {
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


}
