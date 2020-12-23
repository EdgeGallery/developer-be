/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model.workspace;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMepCapabilityGroup {

    private String groupId;

    private String oneLevelName;

    private String twoLevelName;

    private String threeLevelName;

    private EnumOpenMepType type;

    private String description;

    private List<OpenMepCapabilityDetail> capabilityDetailList;

    public OpenMepCapabilityGroup() {
    }

    /**
     * OpenMepCapabilityGroup.
     */
    public OpenMepCapabilityGroup(String id, String oneLevelName, String twoLevelName, String threeLevelName, EnumOpenMepType type) {
        this.groupId = id;
        this.oneLevelName = oneLevelName;
        this.twoLevelName = twoLevelName;
        this.threeLevelName = threeLevelName;
        this.type = type;
    }
}
