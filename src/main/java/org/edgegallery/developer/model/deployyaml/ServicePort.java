package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePort {
    private String port;

    private String targetPort;

    private String nodePort;
}
