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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.apppackage.appd.InputParam;
import org.edgegallery.developer.model.apppackage.appd.NodeTemplate;
import org.edgegallery.developer.model.apppackage.appd.TopologyTemplate;
import org.edgegallery.developer.model.apppackage.appd.VNFNodeProperty;
import org.edgegallery.developer.model.apppackage.appd.appconfiguration.AppServiceProducedDef;
import org.edgegallery.developer.model.apppackage.appd.appconfiguration.AppServiceRequiredDef;
import org.edgegallery.developer.model.apppackage.appd.appconfiguration.ConfigurationProperty;
import org.edgegallery.developer.model.apppackage.appd.groups.PlacementGroup;
import org.edgegallery.developer.model.apppackage.appd.policies.AntiAffinityRule;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.apppackage.constant.InputConstant;
import org.edgegallery.developer.model.apppackage.constant.NodeTypeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public abstract class TopologyTemplateConverter {

    public static final Logger LOGGER = LoggerFactory.getLogger(TopologyTemplateConverter.class);

    // memory unit is Mib
    public static final int MEMORY_SIZE_UNIT = 1024;

    TopologyTemplate topologyTemplate;

    protected void updateVnfNode(String name, String provider, String version) {
        NodeTemplate vnfNode = topologyTemplate.getNodeTemplates().get(AppdConstants.VNF_NODE_NAME);
        VNFNodeProperty vnfNodeProperty = (VNFNodeProperty) vnfNode.getProperties();
        vnfNodeProperty.setVnfdId(name);
        vnfNodeProperty.setProvider(provider);
        vnfNodeProperty.setProductName(name);
        vnfNodeProperty.setSoftwareVersion(version);
    }

    protected String getInputStr(String inputName) {
        return InputConstant.GET_INPUT_PREFIX + inputName + InputConstant.GET_INPUT_POSTFIX;
    }

    public void updateGroupsAndPolicies() {
        //update groups
        if (null == topologyTemplate.getGroups()) {
            topologyTemplate.setGroups(new LinkedHashMap<>());
        }
        PlacementGroup group = new PlacementGroup();
        List<String> members = new ArrayList<>();
        for (Map.Entry<String, NodeTemplate> entry : topologyTemplate.getNodeTemplates().entrySet()) {
            if (entry.getValue().getType().equals(NodeTypeConstant.NODE_TYPE_VDU)) {
                members.add(entry.getKey());
            }
        }
        group.setMembers(members);
        topologyTemplate.getGroups().put(AppdConstants.GROUPS_NODE_NAME, group);
        //update policies;
        if (null == topologyTemplate.getPolicies()) {
            topologyTemplate.setPolicies(new ArrayList<>());
        }
        LinkedHashMap<String, AntiAffinityRule> policyMap = new LinkedHashMap<>();
        AntiAffinityRule rule = new AntiAffinityRule();
        List<String> groupLst = new ArrayList<>();
        groupLst.add(AppdConstants.GROUPS_NODE_NAME);
        rule.setTargets(groupLst);
        policyMap.put(AppdConstants.POLICY_NODE_NAME, rule);
        topologyTemplate.getPolicies().add(policyMap);

    }

    public void updateAppConfiguration(Application app) {
        //if no configuration, skip this node
        AppConfiguration appConfiguration = app.getAppConfiguration();
        if ((null == appConfiguration.getAppServiceProducedList() || appConfiguration.getAppServiceProducedList()
            .isEmpty()) && (null == appConfiguration.getAppServiceRequiredList()
            || appConfiguration.getAppServiceRequiredList().isEmpty()) && (null == appConfiguration.getTrafficRuleList()
            || appConfiguration.getTrafficRuleList().isEmpty()) && (null == appConfiguration.getDnsRuleList()
            || appConfiguration.getDnsRuleList().isEmpty())) {
            return;
        }
        if (null == topologyTemplate.getInputs()) {
            topologyTemplate.setInputs(new LinkedHashMap<String, InputParam>());
        }
        topologyTemplate.getInputs().put(InputConstant.INPUT_NAME_AK,
            new InputParam(InputConstant.TYPE_STRING, "", InputConstant.INPUT_NAME_AK));
        topologyTemplate.getInputs().put(InputConstant.INPUT_NAME_SK,
            new InputParam(InputConstant.TYPE_PASSWORD, "", InputConstant.INPUT_NAME_SK));
        NodeTemplate appConfigurationNode = new NodeTemplate();
        appConfigurationNode.setType(NodeTypeConstant.NODE_TYPE_APP_CONFIGURATIOIN);
        ConfigurationProperty property = new ConfigurationProperty();
        if (!CollectionUtils.isEmpty(appConfiguration.getAppServiceRequiredList())) {
            for (AppServiceRequired appServiceRequired : appConfiguration.getAppServiceRequiredList()) {
                AppServiceRequiredDef def = new AppServiceRequiredDef();
                def.setSerName(appServiceRequired.getSerName());
                def.setAppId(appServiceRequired.getAppId());
                def.setPackageId(appServiceRequired.getPackageId());
                def.setVersion(appServiceRequired.getVersion());
                def.setRequestedPermissions(appServiceRequired.isRequestedPermissions());
                property.getAppServiceRequired().add(def);
            }
        }
        if (!CollectionUtils.isEmpty(appConfiguration.getAppServiceProducedList())) {
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
        topologyTemplate.getNodeTemplates().put(AppdConstants.APP_CONFIGURATION_NODE_NAME, appConfigurationNode);
    }

}
