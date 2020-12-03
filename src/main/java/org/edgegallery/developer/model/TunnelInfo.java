package org.edgegallery.developer.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TunnelInfo {

    private String tunnelType;

    private String tunnelDstAddress;

    private String tunnelSrcAddress;
}
