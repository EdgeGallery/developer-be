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

package org.edgegallery.developer.service.apppackage.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class AppDefinitionConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDefinitionConverter.class);

    private static final String TEMPLATE_CSAR_BASE_PATH = "/APPD/Definition/app-name.yaml";

    public static String getAppdPath(String applicationId, String appPackageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId
            + File.separator + appPackageId + TEMPLATE_CSAR_BASE_PATH;
    }

    public static boolean saveAppdYaml(AppDefinition appDescriptor) {

        return true;
    }

    public static AppDefinition loadAppdYaml(AppPackage appPackage) {
        return null;
    }

    public static AppDefinition convertApplication2Appd(AppPackage appPackage, VMApplication application) {
        AppDefinition appDefinition = new AppDefinition();
        //if the yaml file already exists, read from file as default.
        String appdFilePath = getAppdPath(application.getId(), appPackage.getAppId());
        File file = new File(appdFilePath);
        if (file.exists()) {
            try {
                Yaml yaml = new Yaml();
                appDefinition = yaml.loadAs(new FileInputStream(file), AppDefinition.class);
            } catch (FileNotFoundException e) {
                LOGGER.warn("Appd {} exists, but read failed, will create default APPD file.", appdFilePath);
            }
        }
        //update metadata
        appDefinition.getMetadata().setVnfd_id(application.getName());
        appDefinition.getMetadata().setVnfd_name(application.getName());

        //query flavor list and image List

        //update the nodeTemplates
        appDefinition.getTopology_template().updateVnfNode(application)
            .updateVMs(application.getNetworkList(), application.getVmList(), null, null)
            .updateVLs(application.getNetworkList());

        return appDefinition;
    }

    public static void main(String args[]) {

        StringWriter writer = new StringWriter();
        CustomRepresenter representer = new CustomRepresenter();

        Yaml yaml = new Yaml(representer, new DumperOptions());
        AppDefinition appd = new AppDefinition();
        String res = yaml.dumpAsMap(appd);
        System.out.println(res);
    }
}
