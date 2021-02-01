package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Spec {
    private Containers[] containers;
    private Volumes[] volumes;
    private  String Type;
    private  ServicePorts[] ports;
    private  Selector selector;


}
