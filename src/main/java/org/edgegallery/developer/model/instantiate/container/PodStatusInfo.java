/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model.instantiate.container;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodStatusInfo {

    private String podstatus;

    private String podname;

    private String[] podEventsInfo;

    private PodContainers[] containers;

    /**
     * getPodEventsInfo.
     *
     * @return
     */
    public String[] getPodEventsInfo() {
        if (podEventsInfo != null && podEventsInfo.length > 0) {
            return podEventsInfo.clone();
        }
        return new String[0];
    }

    /**
     * setPodEventsInfo.
     *
     * @param podEventsInfo podEventsInfo
     */
    public void setPodEventsInfo(String[] podEventsInfo) {
        if (podEventsInfo != null && podEventsInfo.length > 0) {
            this.podEventsInfo = podEventsInfo.clone();
        } else {
            this.podEventsInfo = new String[0];
        }
    }

    /**
     * getContainers.
     *
     * @return
     */
    public PodContainers[] getContainers() {
        if (containers != null && containers.length > 0) {
            return containers.clone();
        }
        return new PodContainers[0];
    }

    /**
     * setContainers.
     *
     * @param containers containers
     */
    public void setContainers(PodContainers[] containers) {
        if (containers != null && containers.length > 0) {
            this.containers = containers.clone();
        } else {
            this.containers = new PodContainers[0];
        }
    }

}
