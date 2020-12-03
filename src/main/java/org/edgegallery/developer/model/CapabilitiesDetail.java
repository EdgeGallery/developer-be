package org.edgegallery.developer.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapabilitiesDetail {
    private List<TrafficRule> appTrafficRule;

    private List<DnsRule> appDNSRule;

    private List<ServiceDetail> serviceDetails;

}
