package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DstInterface {

    private String interfaceType;

    private String srcMacAddress;

    private String dstMacAddress;

    private String dstIpAddress;

    private TunnelInfo tunnelInfo;
}
