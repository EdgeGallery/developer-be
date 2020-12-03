package org.edgegallery.developer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenhui
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceDetail {

    private String serviceName;

    private Integer internalPort;

    private String version;

    private String protocal;

    private String apiJson;

    private String apiMd;

    private List<String> dnsRulesList;

    private List<String> trafficRulesList;
}
