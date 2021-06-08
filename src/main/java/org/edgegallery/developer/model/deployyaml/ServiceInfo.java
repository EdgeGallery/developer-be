package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceInfo {

    private String serviceName;

    private String type;

    private ServicePort[] ports;
}
