package org.edgegallery.developer.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceDetail {

    private String serviceName;

    private int internalPort;

    private String version;

    private String protocal;

    private String apiJson;

    private String apiMd;

    private List<String> dnsRulesList;

    private List<String> trafficRulesList;
}
