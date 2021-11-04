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
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.model.resource.vm.Flavor;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.service.recource.vm.FlavorService;
import org.edgegallery.developer.service.recource.vm.VMImageService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

public class AppDefinitionConverter {

    private VMImageService vmImageService = (VMImageService) SpringContextUtil.getBean(VMImageService.class);

    private FlavorService flavorService = (FlavorService) SpringContextUtil.getBean(FlavorService.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDefinitionConverter.class);

    private static final String REGEX_INPUT_RAMA_QUOTES_REMOVE = "('\\s*\\{\\s*get_input\\s*:\\s+)(((?!\\s*}).)+)(\\s*}\\s*')";

    private static final String INPUT_PARAM_REPLACEMENT = "{get_input: $2}";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/APPD/Definition/app-name.yaml";

    public String getAppdPath(String applicationId, String appPackageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId
            + File.separator + appPackageId + TEMPLATE_CSAR_BASE_PATH;
    }

    public boolean saveAppdYaml(String appdFilePath, AppDefinition appDefinition) {
        StringWriter writer = new StringWriter();
        CustomRepresenter representer = new CustomRepresenter();
        Yaml yaml = new Yaml(representer, new DumperOptions());
        String yamlContents = yaml.dumpAs(appDefinition, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
        //get_input function string is a feature not supported by snakeyaml, need to remove the quotes.
        String outPutContent = yamlContents.replaceAll(REGEX_INPUT_RAMA_QUOTES_REMOVE, INPUT_PARAM_REPLACEMENT);
        File file = new File(appdFilePath);
        try {
            FileUtils.writeStringToFile(file, outPutContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Write appd yaml to file failed. app yaml content:\n {}", yaml);
            return false;
        }
        return true;
    }

    public AppDefinition loadAppdYaml(String appdFilePath) {
        AppDefinition appDefinition = null;
        File file = new File(appdFilePath);
        if (file.exists()) {
            try {
                Yaml yaml = new Yaml();
                appDefinition = yaml.loadAs(new FileInputStream(file), AppDefinition.class);
            } catch (FileNotFoundException e) {
                LOGGER.error("Appd {} exists, but read failed, will create default APPD file.", appdFilePath);
                return null;
            }
        }
        return appDefinition;
    }

    public AppDefinition convertApplication2Appd(String appdFilePath, VMApplication application) {
        //if the yaml file already exists, read from file as default.
        AppDefinition appDefinition =  new AppDefinition();
        //update metadata
        appDefinition.getMetadata().setVnfd_id(application.getName());
        appDefinition.getMetadata().setVnfd_name(application.getName());
        //update the nodeTemplates
        appDefinition.getTopology_template()
            .updateNodeTemplates(application, queryFlavors(application), queryImages(application));
        appDefinition.getTopology_template().updateGroupsAndPolicies();
        return appDefinition;
    }

    private Map<String, Flavor> queryFlavors(VMApplication application) {
        Set<String> flavorIds = new HashSet<>();
        for (VirtualMachine vm : application.getVmList()) {
            flavorIds.add(vm.getFlavorId());
        }
        Map<String, Flavor> flavorMap = new HashMap<>();
        for (String flavorId : flavorIds) {
            flavorMap.put(flavorId, flavorService.getFavorById(flavorId));
        }
        return flavorMap;
    }

    private Map<Integer, VMImage> queryImages(VMApplication application) {
        Set<Integer> imageIds = new HashSet<>();
        for (VirtualMachine vm : application.getVmList()) {
            imageIds.add(Integer.valueOf(vm.getImageId()));
        }
        Map<Integer, VMImage> imageMap = new HashMap<>();
        for (Integer imageId : imageIds) {
            imageMap.put(imageId, vmImageService.getVmImageById(imageId));
        }
        return imageMap;
    }

}
