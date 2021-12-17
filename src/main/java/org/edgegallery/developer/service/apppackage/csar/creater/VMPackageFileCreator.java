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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.appd.AppDefinition;
import org.edgegallery.developer.model.apppackage.ImageDesc;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.service.apppackage.csar.appdconverter.AppDefinitionConverter;
import org.edgegallery.developer.service.recource.vm.VMImageService;
import org.edgegallery.developer.util.SpringContextUtil;
import org.springframework.util.CollectionUtils;
import com.google.gson.Gson;

public class VMPackageFileCreator extends PackageFileCreator {

    private VMImageService vmImageService = (VMImageService) SpringContextUtil.getBean(VMImageService.class);

    private VMApplication application;

    private static final String APPD_IMAGE_DES_PATH = "/Image/SwImageDesc.json";


    public VMPackageFileCreator(VMApplication application, String packageId) {
        super(application, packageId);
        this.application = application;

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
        String compressPath = packageFileCompress();
        if (compressPath == null) {
            LOGGER.error("package compress fail");
            return null;
        }
        return compressPath;
    }

    private boolean generateAPPDYaml() {
        String appdFilePath = getAppdFilePath();
        AppDefinitionConverter converter = new AppDefinitionConverter();
        AppDefinition appDefinition = converter.convertApplication2Appd(this.application);
        return converter.saveAppdYaml(appdFilePath, appDefinition);
    }

    @Override
    public void generateImageDesFile() {
        List<ImageDesc> imageDescs = new ArrayList<>();
        for (VirtualMachine vm : application.getVmList()) {
            int imageId = vm.getImageId();
            boolean res = findImageDesByImageId(imageDescs, String.valueOf(imageId));
            if (!res) {
                continue;
            }
            VMImage vmImage = vmImageService.getVmImageById(imageId);
            ImageDesc imageDesc = new ImageDesc(vmImage);
            if (application.getArchitecture().equals("X86")) {
                imageDesc.setArchitecture("x86_64");
            } else if (application.getArchitecture().equals("ARM64")) {
                imageDesc.setArchitecture("aarch64");
            } else {
                imageDesc.setArchitecture("aarch32");
            }
            imageDescs.add(imageDesc);
        }
        // write data into imageJson file
        Gson gson = new Gson();
        File imageJson = new File(getPackagePath() + APPD_IMAGE_DES_PATH);
        writeFile(imageJson, gson.toJson(imageDescs));
    }

    private boolean findImageDesByImageId(List<ImageDesc> imageDescs, String imageId) {
        if(CollectionUtils.isEmpty(imageDescs)) {
            return true;
        }
        for(ImageDesc imageDesc:imageDescs) {
            if (imageDesc.getId().equals(imageId)) {
                return false;
            }
        }
        return true;
    }

}
