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

package org.edgegallery.developer.service.recource.pkgspec.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpecConfig;
import org.edgegallery.developer.service.recource.pkgspec.PkgSpecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PkgSpecServiceImpl implements PkgSpecService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PkgSpecServiceImpl.class);

    private static final String PKG_SPECS_FILE_PATH = "./configs/pkgspecs/pkg_specs.json";

    public List<PkgSpec> getPkgSpecs() {
        PkgSpecConfig pkgSpecConfig = null;
        try {
            File file = new File(PKG_SPECS_FILE_PATH);
            pkgSpecConfig = new ObjectMapper().readValue(file, PkgSpecConfig.class);

        } catch (IOException e) {
            LOGGER.error("Load the package specification file failed.", e);
        }
        if (null != pkgSpecConfig && pkgSpecConfig.isSupportPkgSpecs()) {
            return pkgSpecConfig.getPkgSpecs();
        }
        return new ArrayList<PkgSpec>(0);
    }

    public PkgSpec getPkgSpecById(String pkgSpecId) {
        if (StringUtils.isEmpty(pkgSpecId)) {
            return null;
        }
        List<PkgSpec> pkgSpecs = getPkgSpecs();
        for (PkgSpec pkgSpec : pkgSpecs) {
            if (pkgSpec.getId().equals(pkgSpecId)) {
                return pkgSpec;
            }
        }
        return null;
    }

}
