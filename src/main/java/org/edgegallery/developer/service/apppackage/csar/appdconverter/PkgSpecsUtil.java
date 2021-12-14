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

import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.apppackage.appd.InputParam;
import org.edgegallery.developer.model.apppackage.appd.NodeTemplate;
import org.edgegallery.developer.model.apppackage.appd.TopologyTemplate;
import org.edgegallery.developer.model.apppackage.appd.vdu.VDUCapability;
import org.edgegallery.developer.model.apppackage.appd.vdu.VDUProperty;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.apppackage.constant.InputConstant;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;
import org.edgegallery.developer.model.resource.vm.Flavor;
import org.edgegallery.developer.service.recource.pkgspec.PkgSpecService;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class PkgSpecsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PkgSpecsUtil.class);

    private static final String PKG_SPEC_SUPPORT_DYNAMIC_FLAVOR = "PKG_SPEC_SUPPORT_DYNAMIC_FLAVOR";

    private static final String PKG_SPEC_SUPPORT_FIXED_FLAVOR = "PKG_SPEC_SUPPORT_FIXED_FLAVOR";

    private PkgSpecService pkgSpecService = (PkgSpecService) SpringContextUtil.getBean(PkgSpecService.class);

    @Getter
    @Setter
    private PkgSpec pkgSpec;

    private PkgSpec defaultPkgSpec;

    public void init(String pkgSpecId) {
        pkgSpec = pkgSpecService.getPkgSpecById(pkgSpecId);
        defaultPkgSpec = pkgSpecService.getPkgSpecById(PKG_SPEC_SUPPORT_DYNAMIC_FLAVOR);
    }

    public void updateVDUCapabilities(TopologyTemplate topologyTemplate, String vduName, NodeTemplate vduNode,
        Flavor flavor) {
        if (null != pkgSpec && PKG_SPEC_SUPPORT_FIXED_FLAVOR.equals(pkgSpec.getId())) {
            VDUCapability capability = new VDUCapability(flavor.getMemory() * AppdConstants.MEMORY_SIZE_UNIT,
                flavor.getCpu(), flavor.getArchitecture(), flavor.getSystemDiskSize());
            vduNode.setCapabilities(capability);
        } else {
            String memInputName = vduName + InputConstant.INPUT_MEM_POSTFIX;
            String vCPUInputName = vduName + InputConstant.INPUT_VCPU_POSTFIX;
            String diskInputName = vduName + InputConstant.INPUT_DATADISK_POSTFIX;
            InputParam memInput = new InputParam(InputConstant.TYPE_STRING,
                flavor.getMemory() * AppdConstants.MEMORY_SIZE_UNIT, vduName + InputConstant.INPUT_MEM_DES_POSTFIX);
            InputParam vCPUInput = new InputParam(InputConstant.TYPE_STRING, flavor.getCpu(),
                vduName + InputConstant.INPUT_VCPU_DES_POSTFIX);
            InputParam diskInput = new InputParam(InputConstant.TYPE_STRING, flavor.getSystemDiskSize(),
                vduName + InputConstant.INPUT_DATADISK_DES_POSTFIX);
            topologyTemplate.getInputs().put(vCPUInputName, vCPUInput);
            topologyTemplate.getInputs().put(memInputName, memInput);
            topologyTemplate.getInputs().put(diskInputName, diskInput);
            VDUCapability capability = new VDUCapability(getInputStr(memInputName), getInputStr(vCPUInputName),
                flavor.getArchitecture(), getInputStr(diskInputName));
            vduNode.setCapabilities(capability);
        }
    }

    public void updateFlavorExtraSpecs(TopologyTemplate topologyTemplate, String vduName, VDUProperty property,
        String flavorExtraSpecsStr) {
        LinkedHashMap<String, String> mapSpecs = analyzeVMFlavorExtraSpecs(flavorExtraSpecsStr);
        if (null == mapSpecs) {
            return;
        }
        if (null != pkgSpec && PKG_SPEC_SUPPORT_FIXED_FLAVOR.equals(pkgSpec.getId())) {
            if (mapSpecs.containsKey(InputConstant.FLAVOR_EXTRA_SPECS_HOST_AGGR)) {
                String sgLabel = mapSpecs.get(InputConstant.FLAVOR_EXTRA_SPECS_HOST_AGGR);
                mapSpecs.remove(InputConstant.FLAVOR_EXTRA_SPECS_HOST_AGGR);
                mapSpecs.put(sgLabel, "true");
            }
        } else {
            if (mapSpecs.containsKey(InputConstant.FLAVOR_EXTRA_SPECS_GPU)) {
                String gpuInputName = vduName + InputConstant.INPUT_GPU_POSTFIX;
                String gpuVal = mapSpecs.get(InputConstant.FLAVOR_EXTRA_SPECS_GPU);
                InputParam gpuInput = new InputParam(InputConstant.TYPE_STRING, gpuVal, gpuInputName);
                topologyTemplate.getInputs().put(gpuInputName, gpuInput);
                mapSpecs.replace(InputConstant.FLAVOR_EXTRA_SPECS_GPU, getInputStr(gpuInputName));
            }
            if (mapSpecs.containsKey(InputConstant.FLAVOR_EXTRA_SPECS_HOST_AGGR)) {
                String hostAggrInputName = vduName + InputConstant.INPUT_HOST_AGGR_POSTFIX;
                String hostAggrLabel = mapSpecs.get(InputConstant.FLAVOR_EXTRA_SPECS_HOST_AGGR);
                InputParam hostAggrInput = new InputParam(InputConstant.TYPE_STRING, hostAggrLabel, hostAggrInputName);
                topologyTemplate.getInputs().put(hostAggrInputName, hostAggrInput);
                mapSpecs.replace(InputConstant.FLAVOR_EXTRA_SPECS_HOST_AGGR,
                    getFlavorHostAggrInputStr(hostAggrInputName));
            }
        }
        property.getVduProfile().setFlavorExtraSpecs(mapSpecs);
    }

    public void updateUserDataParam(TopologyTemplate topologyTemplate, LinkedHashMap<String, String> mapPortParams) {
        if (null == pkgSpec || PKG_SPEC_SUPPORT_DYNAMIC_FLAVOR.equals(pkgSpec.getId())) {
            String campusInputName = InputConstant.INPUT_CAMPUS_SEGMENT;
            InputParam campusInput = new InputParam(InputConstant.TYPE_STRING, "", campusInputName);
            if (!topologyTemplate.getInputs().containsKey(campusInputName)) {
                topologyTemplate.getInputs().put(campusInputName, campusInput);
            }
            mapPortParams.put(InputConstant.INPUT_CAMPUS_SEGMENT.toUpperCase(), getInputStr(campusInputName));
        }
    }

    public String getNetworkInternetName() {
        PkgSpec pkgSpecTmp = null == this.pkgSpec ? defaultPkgSpec : pkgSpec;
        return pkgSpecTmp.getSpecifications().getAppdSpecs().getNetworkNameSpecs().getNetworkNameInternet();
    }

    public String getNetworkN6Name() {
        PkgSpec pkgSpecTmp = null == this.pkgSpec ? defaultPkgSpec : pkgSpec;
        return pkgSpecTmp.getSpecifications().getAppdSpecs().getNetworkNameSpecs().getNetworkNameN6();
    }

    public String getNetworkMp1Name() {
        PkgSpec pkgSpecTmp = null == this.pkgSpec ? defaultPkgSpec : pkgSpec;
        return pkgSpecTmp.getSpecifications().getAppdSpecs().getNetworkNameSpecs().getNetworkNameMep();
    }

    public String getDefaultVLName(String networkName) {
        PkgSpec pkgSpecTmp = null == this.pkgSpec ? defaultPkgSpec : pkgSpec;
        if (AppdConstants.DEFAULT_NETWORK_INTERNET.equals(networkName)) {
            return AppdConstants.NETWORK_NAME_PREFIX + pkgSpecTmp.getSpecifications().getAppdSpecs()
                .getNetworkNameSpecs().getNetworkNameInternet();
        } else if (AppdConstants.DEFAULT_NETWORK_N6.equals(networkName)) {
            return AppdConstants.NETWORK_NAME_PREFIX + pkgSpecTmp.getSpecifications().getAppdSpecs()
                .getNetworkNameSpecs().getNetworkNameN6();
        } else {
            return AppdConstants.NETWORK_NAME_PREFIX + pkgSpecTmp.getSpecifications().getAppdSpecs()
                .getNetworkNameSpecs().getNetworkNameMep();
        }
    }

    private LinkedHashMap<String, String> analyzeVMFlavorExtraSpecs(String flavorExtraSpecsStr) {
        if (StringUtils.isEmpty(flavorExtraSpecsStr)) {
            return null;
        }
        //generate the definition for FlavorExtraSpecs
        Yaml yaml = new Yaml(new SafeConstructor());
        LinkedHashMap<String, String> mapSpecs = yaml.load(flavorExtraSpecsStr);
        return mapSpecs;
    }

    private String getInputStr(String inputName) {
        return InputConstant.GET_INPUT_PREFIX + inputName + InputConstant.GET_INPUT_POSTFIX;
    }

    private String getFlavorHostAggrInputStr(String inputName) {
        return "[{\"{get_input:" + inputName + "}\":\"true\"}]";
    }
}
