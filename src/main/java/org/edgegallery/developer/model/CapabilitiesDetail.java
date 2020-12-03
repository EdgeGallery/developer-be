package org.edgegallery.developer.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapabilitiesDetail {
    private List<TrafficRule> trafficRules;

    private List<DnsRule> dnsRules;

    private List<ServiceDetail> serviceDetails;

}
