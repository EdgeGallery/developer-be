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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.appd.vdu.VDUCapability;
import org.edgegallery.developer.model.apppackage.appd.vdu.VDUProperty;
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
    private LinkedHashMap<String, NodeTemplate> nodeTemplates;

    @Valid
    private LinkedHashMap<String, PlacementGroup> groups;

    @Valid
    private List<LinkedHashMap<String, AntiAffinityRule>> policies;

    public TopologyTemplate() {
        initInputs(EnumAppClass.VM);
        initVnfNode();
    }

    public void initInputs(EnumAppClass appClass) {
        if (EnumAppClass.VM.equals(appClass)) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(new File("template/appd/vm_appd_inputs.yaml"));
            } catch (FileNotFoundException e) {
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

    public void initVnfNode() {
        NodeTemplate vnfNode = new NodeTemplate();
        vnfNode.setType(NodeTypeConstant.NODE_TYPE_VNF);
        vnfNode.setProperties(new VNFNodeProperty());
        if (null == this.nodeTemplates) {
            this.nodeTemplates = new LinkedHashMap<String, NodeTemplate>();
        }
        this.nodeTemplates.put(AppdConstants.VNF_NODE_NAME, vnfNode);
    }

    public TopologyTemplate updateVnfNode(VMApplication application) {
        NodeTemplate vnfNode = this.nodeTemplates.get(AppdConstants.VNF_NODE_NAME);
        VNFNodeProperty vnfNodeProperty = (VNFNodeProperty) vnfNode.getProperties();
        vnfNodeProperty.setVnfd_id(application.getName());
        vnfNodeProperty.setProvider(application.getProvider());
        vnfNodeProperty.setProduct_name(application.getName());
        vnfNodeProperty.setSoftware_version(application.getVersion());
        return this;
    }

    public TopologyTemplate updateVLs(List<Network> networkLst) {
        if (null == this.nodeTemplates) {
            this.nodeTemplates = new LinkedHashMap<String, NodeTemplate>();
        }
        for (int i = 0; i < networkLst.size(); i++) {
            //generate inputs for network;
            String networkName = networkLst.get(i).getName();
            int index = i + 1;
            String networkNameInputName = "APP_Plane0" + i + "_Network";
            String networkPhyNetInputName = "APP_Plane0" + i + "_Physnet";
            String networkVlanIdInputName = "APP_Plane0" + i + "_VlanId";
            InputParam networkNameInput = new InputParam(InputConstant.TYPE_STRING, networkName,
                networkLst.get(i).getDescription());
            InputParam networkPhyNet = new InputParam(InputConstant.TYPE_STRING, AppdConstants.DEFALUT_PHYSNET,
                "physical network of " + networkName);
            int vlanId = AppdConstants.DEFALUT_NETWORK_VLANID + i;
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
            vlProfile.setNetworkNameAsInput(networkNameInputName);
            vlProfile.setPhysicalNameAsInput(networkPhyNetInputName);
            vlProfile.setProviderSegmentationNameAsInput(networkVlanIdInputName);
            property.setVl_profile(vlProfile);
            vlNode.setProperties(property);
            this.nodeTemplates.put(networkName, vlNode);
        }
        return this;
    }


    public TopologyTemplate updateVMs(List<VirtualMachine> vmLst, Map<String, Flavor> id2FlavorMap, Map<String, VMImage> id2ImageMap) {
        if (null == this.nodeTemplates) {
            this.nodeTemplates = new LinkedHashMap<String, NodeTemplate>();
        }
        for (int i = 0; i < vmLst.size(); i++) {
            VirtualMachine vm = vmLst.get(i);
            int vduIndex = i + 1;
            String vduName = "EMD_VDU" + vduIndex;
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
            property.setNfviConstraintsAsInput(azInputName);
            property.getVdu_profile().setFlavor_extra_specs(analyzeVMFlavorExtraSpecs(vm.getFlavorExtraSpecs()));
            property.getSw_image_data().setName(id2ImageMap.get(vm.getImageId()).getName());
            property.getBootdata().getUser_data().setContents(vm.getUserData());
            //TODO, params for vdu.
            property.getBootdata().getUser_data().setParams(new LinkedHashMap<String, String>());
            vduNode.setProperties(property);
            this.nodeTemplates.put(vduName, vduNode);
            updateVMPorts(vduName, vm.getPortList());
        }
        return this;
    }

    private LinkedHashMap<String, String> analyzeVMFlavorExtraSpecs(String flavorExtraSpecsStr) {
        //TODO change the flavorExtraSpes to Map.
        return new LinkedHashMap<String, String>();
    }

    private void updateVMPorts(String vduName, List<VMPort> ports) {
        for (VMPort port: ports){
            //generate port inputs
            //String ipInputName =
        }
    }

    private Flavor getFlavor(List<Flavor> flavors, String flavorId) {
        for (Flavor flavor : flavors) {
            if (flavor.getId().equals(flavorId)) {
                return flavor;
            }
        }
        return null;
    }



}
