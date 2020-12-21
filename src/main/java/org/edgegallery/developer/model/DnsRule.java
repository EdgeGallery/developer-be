package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DnsRule {

    private String dnsRuleId;

    private String domainName;

    private String ipAddressType;

    private String ipAddress;

    private String ttl;

}
