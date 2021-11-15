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

package org.edgegallery.developer.util.helmcharts;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.service.apppackage.converter.CustomRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

@Setter
@Getter
public class EgValuesYaml {

    private Global global;

    private AppConfig appConfig;

    private List<ServiceConfig> serviceConfig;

    private ImageLocation imageLocation;

    public static EgValuesYaml createDefaultEgValues() {
        EgValuesYaml defaultValues = new EgValuesYaml();
        Global global = Global.builder().mepAgent(
            Global.MepAgent.builder().enabled(true).configMapName("mepagent-" + UUID.randomUUID().toString()).build())
            .nameSpace(Global.NameSpace.builder().enabled(true).build()).build();
        defaultValues.setGlobal(global);
        defaultValues.setAppConfig(AppConfig.builder().appNameSpace("").aksk(new AppConfig.AkSk()).build());
        defaultValues.setImageLocation(ImageLocation.builder().domainName("192.168.1.1").project("project").build());
        return defaultValues;
    }

    public String getContent() {
        Yaml yaml = new Yaml(new SafeConstructor(), new CustomRepresenter());
        return yaml.dumpAs(this, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
    }

    @Getter
    @Setter
    @Builder
    static class Global {
        private MepAgent mepAgent;

        private NameSpace nameSpace;

        @Getter
        @Setter
        @Builder
        static class MepAgent {
            private boolean enabled;

            private String configMapName;
        }

        @Getter
        @Setter
        @Builder
        static class NameSpace {
            private boolean enabled;
        }

    }

    @Setter
    @Getter
    @Builder
    static class AppConfig {
        private String appNameSpace;

        private AkSk aksk;

        @Getter
        @Setter
        static class AkSk {
            private String secretName = "";

            private String accessKey = "";

            private String secretKey = "";

            private String appInsId = "";
        }
    }

    @Setter
    @Getter
    @Builder
    static class ServiceConfig {
        private String serviceName;

        private int port;

        private String version;

        private String protocol;

        private String appNameSpace;
    }

    @Setter
    @Getter
    @Builder
    static class ImageLocation {
        private String domainName;

        private String project;
    }

}
