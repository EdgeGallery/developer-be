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
import java.util.LinkedHashMap;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.apppackage.appd.groups.PlacementGroup;
import org.edgegallery.developer.model.apppackage.appd.policies.AntiAffinityRule;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.apppackage.constant.NodeTypeConstant;

@Setter
@Getter
public class TopologyTemplate {

    @Valid
    @NotNull
    @JsonProperty("inputs")
    private LinkedHashMap<String, InputParam> inputs = new LinkedHashMap<String, InputParam>();

    @Valid
    @NotNull
    @JsonProperty("node_templates")
    private LinkedHashMap<String, NodeTemplate> nodeTemplates;

    @Valid
    @JsonProperty("groups")
    private LinkedHashMap<String, PlacementGroup> groups;

    @Valid
    @JsonProperty("policies")
    private List<LinkedHashMap<String, AntiAffinityRule>> policies;

    public TopologyTemplate() {
        initVnfNode();
    }

    private void initVnfNode() {
        NodeTemplate vnfNode = new NodeTemplate();
        vnfNode.setType(NodeTypeConstant.NODE_TYPE_VNF);
        vnfNode.setProperties(new VNFNodeProperty());
        if (null == this.nodeTemplates) {
            this.nodeTemplates = new LinkedHashMap<String, NodeTemplate>();
        }
        this.nodeTemplates.put(AppdConstants.VNF_NODE_NAME, vnfNode);
    }
}
