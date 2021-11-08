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

package org.edgegallery.developer.model.apppackage.appd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.appd.appconfiguration.AppServiceProducedDef;
import org.edgegallery.developer.model.apppackage.appd.appconfiguration.ConfigurationProperty;
import org.edgegallery.developer.model.apppackage.appd.groups.PlacementGroup;
import org.edgegallery.developer.model.apppackage.appd.policies.AntiAffinityRule;
import org.edgegallery.developer.model.apppackage.appd.vdu.VDUCapability;
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

@Setter
@Getter
@JsonPropertyOrder(alphabetic = true)
public class TopologyTemplate {

    @Valid
    @NotNull
    private LinkedHashMap<String, InputParam> inputs;

    @Valid
    @NotNull
    @JsonProperty(value = "node_templates")
    private LinkedHashMap<String, NodeTemplate> node_templates;

    @Valid
    private LinkedHashMap<String, PlacementGroup> groups;

    @Valid
    private List<LinkedHashMap<String, AntiAffinityRule>> policies;

    public TopologyTemplate() {
        initInputs(EnumAppClass.VM);
        initVnfNode();
    }

    private void initInputs(EnumAppClass appClass) {
        if (EnumAppClass.VM.equals(appClass)) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(Resources.getResourceAsFile("template/appd/vm_appd_inputs.yaml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Yaml yaml = new Yaml();
            LinkedHashMap<String, LinkedHashMap<String, String>> vmInputs = yaml.load(inputStream);
            if (null == inputs) {
                inputs = new LinkedHashMap<String, InputParam>();
            }
            for (Map.Entry<String, LinkedHashMap<String, String>> entry : vmInputs.entrySet()) {
                inputs.put(entry.getKey(), new InputParam(entry.getValue()));
            }
        }
    }

    private void initVnfNode() {
        NodeTemplate vnfNode = new NodeTemplate();
        vnfNode.setType(NodeTypeConstant.NODE_TYPE_VNF);
        vnfNode.setProperties(new VNFNodeProperty());
        if (null == this.node_templates) {
            this.node_templates = new LinkedHashMap<String, NodeTemplate>();
        }
        this.node_templates.put(AppdConstants.VNF_NODE_NAME, vnfNode);
    }

    public void updateNodeTemplates(VMApplication application, Map<String, Flavor> id2FlavorMap,
        Map<Integer, VMImage> id2ImageMap) {
        updateVnfNode(application);
        updateVMs(application.getNetworkList(), application.getVmList(), id2FlavorMap, id2ImageMap);
        updateVLs(application.getNetworkList());
        updateAppConfiguration(application);
    }

    public void updateGroupsAndPolicies() {
        //update groups
        if (null == groups) {
            groups = new LinkedHashMap<>();
        }
        PlacementGroup group = new PlacementGroup();
        List<String> members = new ArrayList<>();
        for (Map.Entry<String, NodeTemplate> entry : node_templates.entrySet()) {
            if (entry.getValue().getType().equals(NodeTypeConstant.NODE_TYPE_VDU)) {
                members.add(entry.getKey());
            }
        }
        group.setMembers(members);
        groups.put(AppdConstants.GROUPS_NODE_NAME, group);
        //update policies;
        if (null == policies) {
            policies = new ArrayList<>();
        }
        LinkedHashMap<String, AntiAffinityRule> policyMap = new LinkedHashMap<>();
        AntiAffinityRule rule = new AntiAffinityRule();
        List<String> groupLst = new ArrayList<>();
        groupLst.add(AppdConstants.GROUPS_NODE_NAME);
        rule.setTargets(groupLst);
        policyMap.put(AppdConstants.POLICY_NODE_NAME, rule);
        policies.add(policyMap);

    }

    private TopologyTemplate updateVnfNode(VMApplication application) {
        NodeTemplate vnfNode = this.node_templates.get(AppdConstants.VNF_NODE_NAME);
        VNFNodeProperty vnfNodeProperty = (VNFNodeProperty) vnfNode.getProperties();
        vnfNodeProperty.setVnfd_id(application.getName());
        vnfNodeProperty.setProvider(application.getProvider());
        vnfNodeProperty.setProduct_name(application.getName());
        vnfNodeProperty.setSoftware_version(application.getVersion());
        return this;
    }

    private void updateVLs(List<Network> networkLst) {
        if (null == this.node_templates) {
            this.node_templates = new LinkedHashMap<String, NodeTemplate>();
        }
        for (int i = 0; i < networkLst.size(); i++) {
            //generate inputs for network;
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
            this.inputs.put(networkNameInputName, networkNameInput);
            this.inputs.put(networkPhyNetInputName, networkPhyNet);
            this.inputs.put(networkVlanIdInputName, networkVlanId);
            //add VL node
            NodeTemplate vlNode = new NodeTemplate();
            vlNode.setType(NodeTypeConstant.NODE_TYPE_VL);
            VLProperty property = new VLProperty();
            VLProfile vlProfile = new VLProfile();
            vlProfile.setNetwork_name(getInputStr(networkNameInputName));
            vlProfile.setPhysical_network(getInputStr(networkPhyNetInputName));
            vlProfile.setProvider_segmentation_id(getInputStr(networkVlanIdInputName));
            property.setVl_profile(vlProfile);
            vlNode.setProperties(property);
            this.node_templates.put(networkName, vlNode);
        }
    }

    private void updateVMs(List<Network> networkLst, List<VirtualMachine> vmLst, Map<String, Flavor> id2FlavorMap,
        Map<Integer, VMImage> id2ImageMap) {
        if (null == this.node_templates) {
            this.node_templates = new LinkedHashMap<String, NodeTemplate>();
        }
        for (int i = 0; i < vmLst.size(); i++) {
            VirtualMachine vm = vmLst.get(i);
            int vduIndex = i + 1;
            String vduName = InputConstant.VDU_NAME_PREFIX + vduIndex;
            //generate input for VDU
            String azInputName = InputConstant.INPUT_NAME_AZ;
            if (!this.inputs.containsKey(azInputName)) {
                InputParam azInput = new InputParam(InputConstant.TYPE_STRING, vm.getAreaZone(), "az of the vm");
                this.inputs.put(azInputName, azInput);
            }

            //add the VDU node
            NodeTemplate vduNode = new NodeTemplate();
            vduNode.setType(NodeTypeConstant.NODE_TYPE_VDU);
            Flavor flavor = id2FlavorMap.get(vm.getFlavorId());
            VDUCapability capability = new VDUCapability(flavor.getMemory(), flavor.getCpu(), flavor.getArchitecture(),
                flavor.getSystemDiskSize());
            vduNode.setCapabilities(capability);
            VDUProperty property = new VDUProperty();
            property.setName(vm.getName());
            property.setNfvi_constraints(getInputStr(azInputName));
            property.getVdu_profile().setFlavor_extra_specs(analyzeVMFlavorExtraSpecs(vm.getFlavorExtraSpecs()));
            property.getSw_image_data().setName(id2ImageMap.get(Integer.valueOf(vm.getImageId())).getName());
            if (StringUtils.isEmpty(vm.getUserData())) {
                property.getBootdata().setConfig_drive(false);
                property.getBootdata().setUser_data(null);
            } else {
                property.getBootdata().setConfig_drive(true);
                property.getBootdata().getUser_data().setContents(vm.getUserData());
                //params for vdu.
                property.getBootdata().getUser_data()
                    .setParams(getVDUNodeUserDataParams(vduName, vm.getPortList(), networkLst));
            }

            vduNode.setProperties(property);
            this.node_templates.put(vduName, vduNode);
            updateVMPorts(vduName, vm.getPortList(), networkLst);
        }
    }

    private LinkedHashMap<String, String> getVDUNodeUserDataParams(String vduName, List<VMPort> ports,
        List<Network> networkLst) {
        LinkedHashMap<String, String> mapPortParams = new LinkedHashMap<>();
        mapPortParams.put(InputConstant.USER_DATA_PARAM_CERTIFICATE_INFO,
            getInputStr(InputConstant.INPUT_NAME_MAP_CERTIFICATE));
        mapPortParams.put(InputConstant.INPUT_NAME_UE_IP_SEGMENT.toUpperCase(),
            getInputStr(InputConstant.INPUT_NAME_UE_IP_SEGMENT));
        mapPortParams.put(InputConstant.INPUT_NAME_MEP_IP.toUpperCase(), getInputStr(InputConstant.INPUT_NAME_MEP_IP));
        mapPortParams.put(InputConstant.INPUT_NAME_MEP_PORT.toUpperCase(),
            getInputStr(InputConstant.INPUT_NAME_MEP_PORT));
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
            String paramPrefix = "";
            if (port.getNetworkName().equalsIgnoreCase(InputConstant.NETWORK_INTERNET)) {
                paramPrefix = "APP_INTERNET";
            } else if (port.getNetworkName().equalsIgnoreCase(InputConstant.NETWORK_N6)) {
                paramPrefix = "APP_N6";
            } else if (port.getNetworkName().equalsIgnoreCase(InputConstant.NETWORK_MP1)) {
                paramPrefix = "APP_MP1";
            } else {
                paramPrefix = "";
            }
            if (!StringUtils.isEmpty(paramPrefix)) {
                mapPortParams.put(paramPrefix + InputConstant.INPUT_PORT_IP_POSTFIX, getInputStr(portIpInputName));
                mapPortParams.put(paramPrefix + InputConstant.INPUT_PORT_MASK_POSTFIX, getInputStr(portMaskInputName));
                mapPortParams.put(paramPrefix + InputConstant.INPUT_PORT_GW_POSTFIX, getInputStr(portGWInputName));
            }
        }
        return mapPortParams;
    }

    private String getInputStr(String inputName) {
        return InputConstant.GET_INPUT_PREFIX + inputName + InputConstant.GET_INPUT_POSTFIX;
    }

    private LinkedHashMap<String, String> analyzeVMFlavorExtraSpecs(String flavorExtraSpecsStr) {
        if (StringUtils.isEmpty(flavorExtraSpecsStr)) {
            return null;
        }
        //generate Inputs for FlavorExtraSpecs
        int inputIndex = 0;
        while (true) {
            inputIndex = flavorExtraSpecsStr.indexOf(InputConstant.GET_INPUT_PREFIX.trim(), inputIndex + 1);
            if (inputIndex != -1) {
                int inputNameEndIndex = flavorExtraSpecsStr.indexOf(AppdConstants.CLOSING_BRACE_MARK, inputIndex);
                String inputName = flavorExtraSpecsStr.substring(
                    inputIndex + InputConstant.GET_INPUT_PREFIX.trim().length(), inputNameEndIndex).trim();
                InputParam inputParam = new InputParam(InputConstant.TYPE_STRING, "", inputName);
                this.inputs.put(inputName, inputParam);
            } else {
                break;
            }
        }
        //generate the definition for FlavorExtraSpecs
        LinkedHashMap<String, String> mapSpecs = new LinkedHashMap<>();
        String[] specs = flavorExtraSpecsStr.split(AppdConstants.REGEX_LINE_SEPARATOR);
        for (String spec : specs) {
            String lineStr = spec.trim();
            int keyIndex = lineStr.indexOf(AppdConstants.QUOTATION_MARK, 1);
            String key = lineStr.substring(1, keyIndex);
            int valueIndex = lineStr.indexOf(AppdConstants.COLON_MARK, keyIndex);
            String value = lineStr.substring(valueIndex + 1, lineStr.length()).trim();
            if (value.startsWith(AppdConstants.QUOTATION_MARK) || value.startsWith(
                AppdConstants.SINGLE_QUOTATION_MARK)) {
                value = value.substring(1, value.length());
            }
            if (value.endsWith(AppdConstants.QUOTATION_MARK) || value.endsWith(AppdConstants.SINGLE_QUOTATION_MARK)) {
                value = value.substring(0, value.length() - 1);
            }
            mapSpecs.put(key, value);
        }
        return mapSpecs;
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
            this.inputs.put(portIpInputName, ipInput);
            this.inputs.put(portMaskInputName, maskInput);
            this.inputs.put(portGWInputName, gwInput);
            //generate CP node
            NodeTemplate cpNode = new NodeTemplate();
            cpNode.setType(NodeTypeConstant.NODE_TYPE_VDUCP);
            VDUCPProperty property = new VDUCPProperty();
            property.setDescription(getNetworkDescription(networkLst, port.getNetworkName()));
            property.setVnic_name(AppdConstants.PORT_VNIC_NAME_PREFIX + i);
            property.setOrder(i);
            cpNode.setProperties(property);
            VDUCPAttributes attributes = new VDUCPAttributes();
            attributes.setIpv4_address(getInputStr(portIpInputName));
            cpNode.setAttributes(attributes);
            VirtualBindingRequire virtualBinding = new VirtualBindingRequire();
            virtualBinding.setVirtual_binding(vduName);
            VirtualLinkRequire vlRequire = new VirtualLinkRequire();
            vlRequire.setVirtual_link(port.getNetworkName());
            List<Object> requirements = new ArrayList<>();
            requirements.add(virtualBinding);
            requirements.add(vlRequire);
            cpNode.setRequirements(requirements);
            String cpNodeName = vduName + "_CP" + i;
            this.node_templates.put(cpNodeName, cpNode);
        }
    }

    private String getNetworkDescription(List<Network> networkLst, String networkName) {
        for (int i = 0; i < networkLst.size(); i++) {
            if (networkLst.get(i).getName().equals(networkName)) {
                return networkLst.get(i).getDescription();
            }
        }
        return "";
    }

    private int getNetworkIndex(List<Network> networkLst, String networkName) {
        for (int i = 0; i < networkLst.size(); i++) {
            if (networkLst.get(i).getName().equals(networkName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private void updateAppConfiguration(Application app) {
        //if no configuration, skip this node
        AppConfiguration appConfiguration = app.getAppConfiguration();
        if ((null == appConfiguration.getAppServiceProducedList() || appConfiguration.getAppServiceProducedList()
            .isEmpty()) && (null == appConfiguration.getAppServiceRequiredList()
            || appConfiguration.getAppServiceRequiredList().isEmpty()) && (null == appConfiguration.getTrafficRuleList()
            || appConfiguration.getTrafficRuleList().isEmpty()) && (null == appConfiguration.getDnsRuleList()
            || appConfiguration.getDnsRuleList().isEmpty())) {
            return;
        }
        NodeTemplate appConfigurationNode = new NodeTemplate();
        appConfigurationNode.setType(NodeTypeConstant.NODE_TYPE_APP_CONFIGURATIOIN);
        ConfigurationProperty property = new ConfigurationProperty();
        property.setAppCertificate(appConfiguration.getAppCertificate());
        property.setAppServiceRequired(appConfiguration.getAppServiceRequiredList());
        if (null != appConfiguration.getAppServiceProducedList()) {
            for (AppServiceProduced serviceProduced : appConfiguration.getAppServiceProducedList()) {
                AppServiceProducedDef def = new AppServiceProducedDef();
                def.setSerName(serviceProduced.getServiceName());
                def.setVersion(serviceProduced.getVersion());
                def.setTrafficRuleIdList(serviceProduced.getTrafficRuleIdList());
                def.setDnsRuleIdList(serviceProduced.getDnsRuleIdList());
                property.getAppServiceProduced().add(def);
            }
        }
        boolean isNoMp1Call = appConfiguration.getAppServiceProducedList().isEmpty()
            && appConfiguration.getAppServiceRequiredList().isEmpty();
        property.setAppSupportMp1(!isNoMp1Call);
        property.setAppName(app.getName().trim());
        property.setAppTrafficRule(appConfiguration.getTrafficRuleList());
        property.setAppDNSRule(appConfiguration.getDnsRuleList());
        appConfigurationNode.setProperties(property);
        this.node_templates.put(AppdConstants.APP_CONFIGURATION_NODE_NAME, appConfigurationNode);
    }
}
