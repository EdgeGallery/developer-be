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

package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Spec {
    private Containers[] containers;

    private Volumes[] volumes;

    private String type;

    private ServicePorts[] ports;

    private Selector selector;

    /**
     * getContainers.
     *
     * @return
     */
    public Containers[] getContainers() {
        if (containers != null) {
            return containers.clone();
        }
        return new Containers[0];
    }

    /**
     * getVolumes.
     *
     * @return
     */
    public Volumes[] getVolumes() {
        if (volumes != null) {
            return volumes.clone();
        }
        return new Volumes[0];
    }

    /**
     * getPorts.
     *
     * @return
     */
    public ServicePorts[] getPorts() {
        if (ports != null) {
            return ports.clone();
        }
        return new ServicePorts[0];
    }

    /**
     * setContainers.
     *
     * @param containers containers
     */
    public void setContainers(Containers[] containers) {
        if (containers != null) {
            this.containers = containers.clone();
        } else {
            this.containers = null;
        }
    }

    /**
     * setVolumes.
     *
     * @param volumes volumes
     */
    public void setVolumes(Volumes[] volumes) {
        if (volumes != null) {
            this.volumes = volumes.clone();
        } else {
            this.volumes = null;
        }
    }

    /**
     * setPorts.
     *
     * @param ports ports
     */
    public void setPorts(ServicePorts[] ports) {
        if (ports != null) {
            this.ports = ports.clone();
        } else {
            this.ports = null;
        }
    }
}
