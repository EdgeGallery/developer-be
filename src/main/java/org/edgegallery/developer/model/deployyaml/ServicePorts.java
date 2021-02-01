package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePorts {
    private  int port;
    private  int targetPort;
    private  String protocol;
    private  int nodePort;
}
