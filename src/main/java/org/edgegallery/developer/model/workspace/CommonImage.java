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

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommonImage {

    private String imageId;

    private String serviceName;

    private String imageName;

    private String version;

    private List<EnvMap> env;

    private List<PortMap> ports;

    private EnumImagePullPolicy imagePullPolicy;

    private List<VolumeMountMap> volumeMounts;

    public void addPort(int port, int nodePord) {
        addPort(port, "TCP", nodePord);
    }

    /**
     * addPort.
     */
    public void addPort(int port, String protocol, int nodePort) {
        PortMap map = new PortMap(port, protocol, nodePort);
        if (ports == null) {
            ports = new ArrayList<>();
        }
        ports.add(map);
    }

    /**
     * addEnv.
     */
    public void addEnv(String name, String value) {
        EnvMap map = new EnvMap(name, value);
        if (env == null) {
            env = new ArrayList<>();
        }
        env.add(map);
    }

    /**
     * addVolumeMount.
     */
    public void addVolumeMount(String name, String mountPath) {
        VolumeMountMap mountMap = new VolumeMountMap(name, mountPath);
        if (volumeMounts == null) {
            volumeMounts = new ArrayList<>();
        }
        volumeMounts.add(mountMap);
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class EnvMap {

        private String name;

        private String value;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class PortMap {

        private int containerPort;

        private String protocol;

        private int nodePort;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class VolumeMountMap {

        private String name;

        private String mountPath;
    }

}
