package org.edgegallery.developer.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DnsRule {

    private String dnsRuleId;

    private String domainName;

    private String ipAddressType;

    private String dnsServerIp;

    private String ttl;

}
