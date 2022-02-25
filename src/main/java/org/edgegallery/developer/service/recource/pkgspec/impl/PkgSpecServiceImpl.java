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
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpecConfig;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpecConstants;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.recource.pkgspec.PkgSpecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pkgSpecService")
public class PkgSpecServiceImpl implements PkgSpecService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PkgSpecServiceImpl.class);

    private static final String PKG_SPECS_FILE_PATH = "./configs/pkgspecs/pkg_specs.json";

    private static final String DEFAULT_USE_SCENES = "edgeGallery";

    @Autowired
    private VMAppNetworkService vmAppNetworkService;

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
        return Collections.emptyList();
    }

    public PkgSpec getPkgSpecById(String pkgSpecId) {
        String pkgSpecIdTmp = pkgSpecId;
        if (StringUtils.isEmpty(pkgSpecId)) {
            pkgSpecIdTmp = PkgSpecConstants.PKG_SPEC_SUPPORT_DYNAMIC_FLAVOR;
        }
        List<PkgSpec> pkgSpecs = getPkgSpecs();
        for (PkgSpec pkgSpec : pkgSpecs) {
            if (pkgSpec.getId().equals(pkgSpecIdTmp)) {
                return pkgSpec;
            }
        }
        return null;
    }

    @Override
    public List<Network> getNetworkResourceByPkgSpecId(String pkgSpecId) {
        List<Network> networks = vmAppNetworkService.getAllNetwork("init-application");
        PkgSpec pkgSpec = getPkgSpecById(pkgSpecId);
        for (Network network : networks) {
            network.setName(getDefaultVLName(pkgSpec, network.getName()));
        }
        return networks;
    }

    @Override
    public String getUseScenes() {
        PkgSpecConfig pkgSpecConfig = null;
        try {
            File file = new File(PKG_SPECS_FILE_PATH);
            pkgSpecConfig = new ObjectMapper().readValue(file, PkgSpecConfig.class);

        } catch (IOException e) {
            LOGGER.error("Load the package specification file failed.", e);
        }
        if (null != pkgSpecConfig && StringUtils.isNotEmpty(pkgSpecConfig.getUseScenes())) {
            return pkgSpecConfig.getUseScenes();
        }
        return DEFAULT_USE_SCENES;
    }

    private String getDefaultVLName(PkgSpec pkgSpec, String networkName) {
        if (AppdConstants.DEFAULT_NETWORK_INTERNET.equals(networkName)) {
            return AppdConstants.NETWORK_NAME_PREFIX + pkgSpec.getSpecifications().getAppdSpecs().getNetworkNameSpecs()
                .getNetworkNameInternet();
        } else if (AppdConstants.DEFAULT_NETWORK_N6.equals(networkName)) {
            return AppdConstants.NETWORK_NAME_PREFIX + pkgSpec.getSpecifications().getAppdSpecs().getNetworkNameSpecs()
                .getNetworkNameN6();
        } else {
            return AppdConstants.NETWORK_NAME_PREFIX + pkgSpec.getSpecifications().getAppdSpecs().getNetworkNameSpecs()
                .getNetworkNameMep();
        }
    }
}
