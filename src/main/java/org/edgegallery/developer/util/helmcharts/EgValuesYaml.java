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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class EgValuesYaml {

    private Global global;

    private AppConfig appConfig;

    private List<ServiceConfig> serviceConfig;

    private ImageLocation imageLocation;

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
