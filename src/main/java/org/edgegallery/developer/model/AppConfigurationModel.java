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

package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;

@NoArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppConfigurationModel {


    private String type = "tosca.nodes.nfv.app.configuration";

    private ConfigurationProperties properties;

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConfigurationProperties {
        private List<ServiceRequired> appServiceRequired;

        private List<ServiceProduced> appServiceProduced;

        private boolean appSupportMp1 = true;

        private String appName;

        private List<TrafficRule> appTrafficRule;

        private List<DnsRule> appDNSRule;
    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceProduced {
        private String serName;

        private String version;

        private List<String> dnsRuleIdList;

        private List<String> trafficRuleIdList;
    }

    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceRequired {
        private String serName;

        private String version;

        private boolean requestedPermissions = true;

        private String appId;

        private String packageId;

    }


}
