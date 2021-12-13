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

package org.edgegallery.developer.service.recource.vm.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;
import org.edgegallery.developer.service.recource.pkgspec.PkgSpecService;
import org.edgegallery.developer.service.recource.vm.UserDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDataServiceImpl implements UserDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataServiceImpl.class);

    private static final String USER_DATE_TEMPLATE = "./configs/template/user_data/";

    private static final String FILE_SUFFIX = ".txt";

    @Autowired
    PkgSpecService pkgSpecService;

    @Override
    public String getUserData(String osType, String pkgSpecId) {
        String fileName = osType;
        PkgSpec pkgSpec = pkgSpecService.getPkgSpecById(pkgSpecId);
        if (null != pkgSpec) {
            String userDataFlag = pkgSpec.getSpecifications().getAppdSpecs().getUserDataFlag();
            if (!StringUtils.isEmpty(userDataFlag)) {
                fileName = fileName + "_" + userDataFlag;
            }
        }
        try {
            File userDataFile = new File(USER_DATE_TEMPLATE + fileName + FILE_SUFFIX);
            if (!userDataFile.exists()) {
                return null;
            }
            return FileUtils.readFileToString(userDataFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("read user date fail");
            return null;
        }
    }

}
