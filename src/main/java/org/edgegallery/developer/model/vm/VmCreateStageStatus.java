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

package org.edgegallery.developer.model.vm;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;

@Getter
@Setter
@NoArgsConstructor
public class VmCreateStageStatus {

    private EnumTestConfigStatus hostInfo;

    private EnumTestConfigStatus csar;

    private EnumTestConfigStatus instantiateInfo;

    private EnumTestConfigStatus workStatus;

    public static List<String> getOrderedStage() {
        return ImmutableList.of("hostInfo", "csar", "instantiateInfo", "workStatus");
    }

    /**
     * getNextStage.
     */
    public static String getNextStage(String currentStage) {
        if ("workStatus".equalsIgnoreCase(currentStage) || !getOrderedStage().contains(currentStage)) {
            return null;
        }
        int currentIndex = getOrderedStage().indexOf(currentStage);
        return getOrderedStage().get(currentIndex + 1);
    }

}
