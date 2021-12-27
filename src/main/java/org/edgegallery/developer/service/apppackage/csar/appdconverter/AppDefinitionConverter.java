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

package org.edgegallery.developer.service.apppackage.csar.appdconverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.ImageDesc;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.model.apppackage.appd.TopologyTemplate;
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

    private static final String REGEX_INPUT_RAMA_QUOTES_REMOVE
        = "('\\s*\\{\\s*get_input\\s*:\\s+)(((?!\\s*}).)+)(\\s*}\\s*')";

    private static final String INPUT_PARAM_REPLACEMENT = "{get_input: $2}";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/APPD/Definition/app-name.yaml";

    private static final String PACKAGE_NODE_TYPE_TEMPLATE_PATH = "./configs/template/package_template/APPD/Definition";

    public String getAppdPath(String applicationId, String appPackageId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + applicationId
            + File.separator + appPackageId + TEMPLATE_CSAR_BASE_PATH;
    }

    public boolean saveAppdYaml(String appdFilePath, AppDefinition appDefinition) {
        //convert to Map Object. gson will change int to double ,so we use jackson.
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() { };
        Map<String, Object> yamlObject = null;
        try {
            String appdJsonStr = mapper.writeValueAsString(appDefinition);
            yamlObject = mapper.readValue(appdJsonStr, typeRef);
        } catch (JsonProcessingException e) {
            LOGGER.error("Convert yaml object failed", e);
            return false;
        }
        //Dump appd object to yaml .
        CustomRepresenter representer = new CustomRepresenter();
        Yaml yaml = new Yaml(representer, new DumperOptions());
        String yamlContents = yaml.dumpAs(yamlObject, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
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

    public AppDefinition convertApplication2Appd(VMApplication application) {
        AppDefinition appDefinition = new AppDefinition();
        //update metadata
        appDefinition.getMetadata().setVnfdId(application.getName());
        appDefinition.getMetadata().setVnfdName(application.getName());

        appDefinition.setImports(Arrays.asList(getImportNodeTypeFileName()));
        //update the nodeTemplates
        TopologyTemplate topologyTemplate = new VMAppTopologyTemplateConverter().convertNodeTemplates(application,
            queryFlavors(application), queryImages(application));
        appDefinition.setTopologyTemplate(topologyTemplate);
        return appDefinition;
    }

    public AppDefinition convertApplication2Appd(ContainerApplication application, List<ImageDesc> imageDescList) {
        AppDefinition appDefinition = new AppDefinition();
        //update metadata
        appDefinition.getMetadata().setVnfdId(application.getName());
        appDefinition.getMetadata().setVnfdName(application.getName());

        //update the nodeTemplates
        TopologyTemplate topologyTemplate = new ContainerAppTopologyTemplateConverter().convertNodeTemplates(
            application, imageDescList);
        appDefinition.setTopologyTemplate(topologyTemplate);

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
            if (vm.getTargetImageId()!=null) {
                imageIds.add(vm.getTargetImageId());
            }else {
                imageIds.add(vm.getImageId());
            }
        }
        Map<Integer, VMImage> imageMap = new HashMap<>();
        for (Integer imageId : imageIds) {
            imageMap.put(imageId, vmImageService.getVmImageById(imageId));
        }
        return imageMap;
    }

    private static String getImportNodeTypeFileName(){
        File file  = new File(PACKAGE_NODE_TYPE_TEMPLATE_PATH);
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files.length == 1){
                return files[0].getName();
            }
        }
        return "nfv_vnfd_types_v1_0.yaml";
    }
}
