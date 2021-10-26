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
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.service.apppackage.converter.AppDefinitionConverter;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;

public class VMPackageFileCreator extends PackageFileCreator {

    private VMApplication application;

    private String packageId;

    private static final String APPD_BASE_PATH = "/APPD/Definition/";

    private static final String APPD_FILE_TYPE = ".yaml";

    public String getAppdFilePath() {
        return getPackageBasePath() + APPD_BASE_PATH + application.getName() + APPD_FILE_TYPE;
    }

    public VMPackageFileCreator(VMApplication application, String packageId) throws IOException {
        super(application, packageId);
        this.application = application;
        this.packageId = packageId;

    }

    public String generateAppPackageFile() {
        String packagePath = getPackageBasePath();
        generateAPPDYaml();
        generateImageDesFile();
        return packagePath;
    }

    private boolean generateAPPDYaml() {
        String appdFilePath = getAppdFilePath();
        AppDefinitionConverter converter = new AppDefinitionConverter();
        AppDefinition appDefinition = converter.convertApplication2Appd(appdFilePath, this.application);
        return converter.saveAppdYaml(appdFilePath, appDefinition);
    }

    private File generateImageDesFile() {

        return new File(getPackageBasePath());

    }

}
