package org.edgegallery.developer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DstInterface {

    private String interfaceType;

    private String srcMacAddress;

    private String dstMacAddress;

    private String dstIpAddress;

    private TunnelInfo tunnelInfo;
}
