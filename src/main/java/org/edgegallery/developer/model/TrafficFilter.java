package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenhui
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficFilter {

    private String ipAddressType;

    private String protocal;

    private String srcAddress;

    private Integer qCI;

    private String srcPort;

    private Integer dSCP;

    private String dstAddress;

    private Integer tC;

    private String dstPort;

    private String tag;

    private String srcTunnelAddress;

    private String tgtTunnelAddress;

    private String srcTunnelPort;

    private String dstTunnelPort;

}
