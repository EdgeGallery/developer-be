package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceDetail {

    private  String groupId;

    private String serviceName;

    private Integer internalPort;

    private String version;

    private String protocol;

    private String apiJson;

    private String apiMd;

    private List<String> dnsRulesList;

    private List<String> trafficRulesList;
}
