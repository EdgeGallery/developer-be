package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
