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
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.util.APPDParserUtil;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;


public class VMPackageFileCreator {

    private static final String APPD_TEMPLATE_PATH = "template/appd/vm_appd_template.yaml";

    private static final String PACKAGE_BASE_PATH = "APPD";


    private VMApplication application;

    private String packageId;

    public VMPackageFileCreator(VMApplication application, String packageId) {
        this.application = application;
        this.packageId = packageId;

    }

    public String generateAppPackageFile() {
        String packagePath = getPackageBasePath();
        return packagePath;
    }

    private File generateAPPDYaml() throws IOException {
        File vmAPPDTemplate = new File(APPD_TEMPLATE_PATH);
        String yamlContent = FileUtils.readFileToString(vmAPPDTemplate, StandardCharsets.UTF_8);
        AppDefinition definition = APPDParserUtil.parseAppd(yamlContent);
        return new File(getPackageBasePath());
    }

    AppDefinition convertApplication2AppDefinition(VMApplication application){
        AppDefinition appDefinition = new AppDefinition();
        //update metadata
        appDefinition.getMetadata().setVnfd_id(application.getName());
        appDefinition.getMetadata().setVnfd_name(application.getName());

        //update
        return appDefinition;
    }

    public void updateMetadata(){

    }



    private File generateImageDesFile() {

        return new File(getPackageBasePath());

    }

    private String getPackageBasePath() {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + application.getId()
            + File.separator + packageId;
    }
}
