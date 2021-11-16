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
        defaultValues.setGlobal(Global.builder().build());
        defaultValues.setAppConfig(AppConfig.builder().build());
        defaultValues.setImageLocation(ImageLocation.builder().build());
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
        @Builder.Default
        private MepAgent mepAgent = MepAgent.builder().build();

        @Builder.Default
        private NameSpace nameSpace = NameSpace.builder().build();

        @Getter
        @Setter
        @Builder
        static class MepAgent {
            @Builder.Default
            private boolean enabled = true;

            @Builder.Default
            private String configMapName = "mepagent-" + UUID.randomUUID().toString();
        }

        @Getter
        @Setter
        @Builder
        static class NameSpace {
            @Builder.Default
            private boolean enabled = true;
        }
    }

    @Setter
    @Getter
    @Builder
    static class AppConfig {
        @Builder.Default
        private String appNameSpace = "<NAMESPACE>";

        @Builder.Default
        private AkSk aksk = new AkSk();

        @Getter
        @Setter
        static class AkSk {

            private String secretName = "<random_value>";

            private String accessKey = "<akvalue>";

            private String secretKey = "<skvalue>";

            private String appInsId = "<idvalue>";
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
        @Builder.Default
        private String domainName = "192.168.1.1";

        @Builder.Default
        private String project = "project";
    }

}
