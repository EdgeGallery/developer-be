package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Containers {
    private String name;

    private String image;

    private String imagePullPolicy;

    private Environment[] env;

    private Ports[] ports;

    private VolumeMounts[] volumeMounts;

    private String[] command;

}
