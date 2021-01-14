package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TunnelInfo {

    private String tunnelType;

    private String tunnelDstAddress;

    private String tunnelSrcAddress;

    private String tunnelSpecificData;
}
