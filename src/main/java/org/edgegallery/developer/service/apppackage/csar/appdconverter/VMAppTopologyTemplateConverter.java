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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.appd.InputParam;
import org.edgegallery.developer.model.apppackage.appd.NodeTemplate;
import org.edgegallery.developer.model.apppackage.appd.TopologyTemplate;
import org.edgegallery.developer.model.apppackage.appd.vdu.VDUProperty;
import org.edgegallery.developer.model.apppackage.appd.vducp.VDUCPAttributes;
import org.edgegallery.developer.model.apppackage.appd.vducp.VDUCPProperty;
import org.edgegallery.developer.model.apppackage.appd.vducp.VirtualBindingRequire;
import org.edgegallery.developer.model.apppackage.appd.vducp.VirtualLinkRequire;
import org.edgegallery.developer.model.apppackage.appd.vl.VLProfile;
import org.edgegallery.developer.model.apppackage.appd.vl.VLProperty;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.apppackage.constant.InputConstant;
import org.edgegallery.developer.model.apppackage.constant.NodeTypeConstant;
import org.edgegallery.developer.model.resource.vm.Flavor;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class VMAppTopologyTemplateConverter extends TopologyTemplateConverter {

    private static final String VM_PACKAGE_TEMPLATE_INPUT_PATH = "./configs/template/appd/vm_appd_inputs.yaml";

    PkgSpecsUtil pkgSpecsUtil;

    public VMAppTopologyTemplateConverter() {
        topologyTemplate = new TopologyTemplate();
        pkgSpecsUtil = new PkgSpecsUtil();
    }

    public TopologyTemplate convertNodeTemplates(VMApplication application, Map<String, Flavor> id2FlavorMap,
        Map<Integer, VMImage> id2ImageMap) {
        pkgSpecsUtil.init(application.getPkgSpecId());
        initVmInputs();
        updateVnfNode(application.getName(), application.getProvider(), application.getVersion());
        updateVDUs(application, id2FlavorMap, id2ImageMap);
        updateVLs(application.getNetworkList());
        updateAppConfiguration(application);
        updateGroupsAndPolicies();
        // set default input data
        setDefaultInputData(this.topologyTemplate.getInputs());
        return this.topologyTemplate;
    }

    private void setDefaultInputData(LinkedHashMap<String, InputParam> inputs) {
        for (Map.Entry<String, InputParam> entry : inputs.entrySet()) {

            Object defaultVal = VmDefaultInputData.getDefaultData(entry.getKey());
            if (defaultVal != null) {
                entry.getValue().setDefaultValue(defaultVal);
            }
//            if (value == null || StringUtils.isEmpty(value.toString())) {
//                Object defaultVal = VmDefaultInputData.getDefaultData(entry.getKey());
//                if (defaultVal != null) {
//                    entry.getValue().setDefaultValue(defaultVal);
//                }
//            }
        }
    }

    private void initVmInputs() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(VM_PACKAGE_TEMPLATE_INPUT_PATH));
        } catch (IOException e) {
            LOGGER.error("init vm inputs read file failed. {}", e.getMessage());
            throw new FileOperateException("init vm inputs read file failed.", ResponseConsts.RET_LOAD_YAML_FAIL);
        }
        Yaml yaml = new Yaml(new SafeConstructor());
        LinkedHashMap<String, LinkedHashMap<String, String>> vmInputs = yaml.load(inputStream);
        for (Map.Entry<String, LinkedHashMap<String, String>> entry : vmInputs.entrySet()) {
            topologyTemplate.getInputs().put(entry.getKey(), new InputParam(entry.getValue()));
        }
    }

    protected void updateVLs(List<Network> networkLst) {
        if (null == topologyTemplate.getNodeTemplates()) {
            topologyTemplate.setNodeTemplates(new LinkedHashMap<>());
        }
        for (int i = 0; i < networkLst.size(); i++) {
            //generate inputs for network
            String networkName = networkLst.get(i).getName();
            int index = i + 1;
            String networkNameInputName = InputConstant.INPUT_NETWORK_PREFIX + index
                + InputConstant.INPUT_NETWORK_POSTFIX;
            String networkPhyNetInputName = InputConstant.INPUT_NETWORK_PREFIX + index
                + InputConstant.INPUT_PHYSNET_POSTFIX;
            String networkVlanIdInputName = InputConstant.INPUT_NETWORK_PREFIX + index
                + InputConstant.INPUT_VLANID_POSTFIX;
            InputParam networkNameInput = new InputParam(InputConstant.TYPE_STRING, networkName,
                networkLst.get(i).getDescription());
            InputParam networkPhyNet = new InputParam(InputConstant.TYPE_STRING, InputConstant.DEFALUT_PHYSNET,
                "physical network of " + networkName);
            int vlanId = InputConstant.DEFALUT_NETWORK_VLANID + index;
            InputParam networkVlanId = new InputParam(InputConstant.TYPE_STRING, String.valueOf(vlanId),
                "vlan id of " + networkName);
            topologyTemplate.getInputs().put(networkNameInputName, networkNameInput);
            topologyTemplate.getInputs().put(networkPhyNetInputName, networkPhyNet);
            topologyTemplate.getInputs().put(networkVlanIdInputName, networkVlanId);
            //add VL node
            NodeTemplate vlNode = new NodeTemplate();
            vlNode.setType(NodeTypeConstant.NODE_TYPE_VL);
            VLProperty property = new VLProperty();
            VLProfile vlProfile = new VLProfile();
            vlProfile.setNetworkName(getInputStr(networkNameInputName));
            vlProfile.setPhysicalNetwork(getInputStr(networkPhyNetInputName));
            vlProfile.setProviderSegmentationId(getInputStr(networkVlanIdInputName));
            property.setVlProfile(vlProfile);
            vlNode.setProperties(property);
            topologyTemplate.getNodeTemplates().put(networkName, vlNode);
        }
    }

    protected void updateVDUs(VMApplication application, Map<String, Flavor> id2FlavorMap,
        Map<Integer, VMImage> id2ImageMap) {
        List<Network> networkLst = application.getNetworkList();
        List<VirtualMachine> vmLst = application.getVmList();
        if (null == topologyTemplate.getNodeTemplates()) {
            topologyTemplate.setNodeTemplates(new LinkedHashMap<>());
        }
        for (int i = 0; i < vmLst.size(); i++) {
            VirtualMachine vm = vmLst.get(i);
            int vduIndex = i + 1;
            String vduName = InputConstant.VDU_NAME_PREFIX + vduIndex;
            //generate input for VDU
            String azInputName = InputConstant.INPUT_NAME_AZ;
            if (!topologyTemplate.getInputs().containsKey(azInputName)) {
                InputParam azInput = new InputParam(InputConstant.TYPE_STRING, vm.getAreaZone(), "az of the vm");
                topologyTemplate.getInputs().put(azInputName, azInput);
            }

            //add the VDU node
            NodeTemplate vduNode = new NodeTemplate();
            vduNode.setType(NodeTypeConstant.NODE_TYPE_VDU);
            Flavor flavor = id2FlavorMap.get(vm.getFlavorId());
            pkgSpecsUtil.updateVDUCapabilities(topologyTemplate, vduName, vduNode, flavor);
            VDUProperty property = new VDUProperty();
            property.setName(vm.getName());
            property.setNfviConstraints(getInputStr(azInputName));
            pkgSpecsUtil.updateFlavorExtraSpecs(topologyTemplate, vduName, property, vm.getFlavorExtraSpecs());
            if (vm.getTargetImageId() != null) {
                property.getSwImageData().setName(id2ImageMap.get(vm.getTargetImageId()).getName());
            } else {
                property.getSwImageData().setName(id2ImageMap.get(vm.getImageId()).getName());
            }
            if (StringUtils.isEmpty(vm.getUserData())) {
                property.getBootdata().setConfigDrive(false);
                property.getBootdata().setUserData(null);
            } else {
                property.getBootdata().setConfigDrive(true);
                property.getBootdata().getUserData().setContents(vm.getUserData());
                //params for vdu.
                property.getBootdata().getUserData()
                    .setParams(getVDUNodeUserDataParams(vduName, vm.getPortList(), networkLst));
            }

            vduNode.setProperties(property);
            topologyTemplate.getNodeTemplates().put(vduName, vduNode);
            updateVMPorts(vduName, vm.getPortList(), networkLst);
        }
    }

    private LinkedHashMap<String, String> getVDUNodeUserDataParams(String vduName, List<VMPort> ports,
        List<Network> networkLst) {
        LinkedHashMap<String, String> mapPortParams = new LinkedHashMap<>();
        mapPortParams.put(InputConstant.INPUT_NAME_UE_IP_SEGMENT.toUpperCase(),
            getInputStr(InputConstant.INPUT_NAME_UE_IP_SEGMENT));
        pkgSpecsUtil.updateUserDataParam(topologyTemplate, mapPortParams);
        for (int i = 0; i < ports.size(); i++) {
            //generate port inputs
            VMPort port = ports.get(i);
            int networkIndex = getNetworkIndex(networkLst, port.getNetworkName());
            String portIpInputName = vduName + "_" + InputConstant.INPUT_NETWORK_PREFIX + networkIndex
                + InputConstant.INPUT_PORT_IP_POSTFIX;
            String portMaskInputName = vduName + "_" + InputConstant.INPUT_NETWORK_PREFIX + networkIndex
                + InputConstant.INPUT_PORT_MASK_POSTFIX;
            String portGWInputName = vduName + "_" + InputConstant.INPUT_NETWORK_PREFIX + networkIndex
                + InputConstant.INPUT_PORT_GW_POSTFIX;
            String paramPrefix = port.getNetworkName().replaceAll(AppdConstants.NETWORK_NAME_PREFIX, "");
            if (!StringUtils.isEmpty(paramPrefix)) {
                mapPortParams.put(paramPrefix + InputConstant.INPUT_PORT_IP_POSTFIX, getInputStr(portIpInputName));
                mapPortParams.put(paramPrefix + InputConstant.INPUT_PORT_MASK_POSTFIX, getInputStr(portMaskInputName));
                mapPortParams.put(paramPrefix + InputConstant.INPUT_PORT_GW_POSTFIX, getInputStr(portGWInputName));
            }
        }
        return mapPortParams;
    }

    private void updateVMPorts(String vduName, List<VMPort> ports, List<Network> networkLst) {
        for (int i = 0; i < ports.size(); i++) {
            //generate port inputs
            VMPort port = ports.get(i);
            int networkIndex = getNetworkIndex(networkLst, port.getNetworkName());
            String portIpInputName = vduName + "_" + InputConstant.INPUT_NETWORK_PREFIX + networkIndex
                + InputConstant.INPUT_PORT_IP_POSTFIX;
            String portMaskInputName = vduName + "_" + InputConstant.INPUT_NETWORK_PREFIX + networkIndex
                + InputConstant.INPUT_PORT_MASK_POSTFIX;
            String portGWInputName = vduName + "_" + InputConstant.INPUT_NETWORK_PREFIX + networkIndex
                + InputConstant.INPUT_PORT_GW_POSTFIX;
            InputParam ipInput = new InputParam(InputConstant.TYPE_STRING, "", portIpInputName);
            InputParam maskInput = new InputParam(InputConstant.TYPE_STRING, InputConstant.INPUT_PORT_MASK_DEFAULT,
                portMaskInputName);
            InputParam gwInput = new InputParam(InputConstant.TYPE_STRING, "", portGWInputName);
            topologyTemplate.getInputs().put(portIpInputName, ipInput);
            topologyTemplate.getInputs().put(portMaskInputName, maskInput);
            topologyTemplate.getInputs().put(portGWInputName, gwInput);
            //generate CP node
            NodeTemplate cpNode = new NodeTemplate();
            cpNode.setType(NodeTypeConstant.NODE_TYPE_VDUCP);
            VDUCPProperty property = new VDUCPProperty();
            property.setDescription(getNetworkDescription(networkLst, port.getNetworkName()));
            property.setVnicName(AppdConstants.PORT_VNIC_NAME_PREFIX + i);
            property.setOrder(i);
            cpNode.setProperties(property);
            VDUCPAttributes attributes = new VDUCPAttributes();
            attributes.setIpv4Address(getInputStr(portIpInputName));
            cpNode.setAttributes(attributes);
            VirtualBindingRequire virtualBinding = new VirtualBindingRequire();
            virtualBinding.setVirtualBinding(vduName);
            VirtualLinkRequire vlRequire = new VirtualLinkRequire();
            vlRequire.setVirtualLink(port.getNetworkName());
            List<Object> requirements = new ArrayList<>();
            requirements.add(virtualBinding);
            requirements.add(vlRequire);
            cpNode.setRequirements(requirements);
            String cpNodeName = vduName + "_CP" + i;
            topologyTemplate.getNodeTemplates().put(cpNodeName, cpNode);
        }
    }

    private int getNetworkIndex(List<Network> networkLst, String networkName) {
        for (int i = 0; i < networkLst.size(); i++) {
            if (networkLst.get(i).getName().equals(networkName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private String getNetworkDescription(List<Network> networkLst, String networkName) {
        for (int i = 0; i < networkLst.size(); i++) {
            if (networkLst.get(i).getName().equals(networkName)) {
                return networkLst.get(i).getDescription();
            }
        }
        return "";
    }
}
