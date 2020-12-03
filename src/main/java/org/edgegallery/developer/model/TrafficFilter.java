package org.edgegallery.developer.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TrafficFilter {

    private String[] protocal;

    private String[] srcAddress;

    private int qCI;

    private String[] srcPort;

    private int dSCP;

    private String[] dstAddress;

    private int tC;

    private String[] dstPort;

    private String[] tag;

    private String[] srcTunnelAddress;

    private String[] tgtTunnelAddress;

    private String[] srcTunnelPort;

    private String[] dstTunnelPort;

}
