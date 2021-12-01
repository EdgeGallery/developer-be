/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InitConstructor {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitConstructor.class);

    @PostConstruct
    public void initProfileData() {
        String profileFilePath = InitConfigUtil.getWorkSpaceBaseDir().concat("configs/profile/");
        String baseFile = InitConfigUtil.getWorkSpaceBaseDir().concat(BusinessConfigUtil.getProfileFilePath());
        File fileList = new File(profileFilePath);
        if (!fileList.exists() || null == fileList.listFiles()) {
            return;
        }
        Arrays.stream(fileList.listFiles()).forEach(file -> {
            try {
                if (file.isDirectory()) {
                    FileUtils.copyDirectory(file, new File(baseFile.concat(file.getName())));
                } else {
                    File targetFile = new File(baseFile.concat(file.getName()));
                    FileUtils.copyFile(file, targetFile);
                }
            } catch (IOException e) {
                LOGGER.error("init profile data failed {}.", e);
            }
        });
    }
}
