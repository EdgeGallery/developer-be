package org.edgegallery.developer.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TrafficFilter {

    private String ipAddressType;

    private String protocal;

    private String srcAddress;

    private int qci;

    private String srcPort;

    private int dscp;

    private String dstAddress;

    private int tc;

    private String dstPort;

}
