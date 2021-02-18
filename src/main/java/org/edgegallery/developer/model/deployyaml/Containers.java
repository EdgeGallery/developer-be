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

    private String command;

    private  Resource resource;

    /**
     * set image.
     * @param image param image
     */
    public void setImage(String image) {
        this.image = "{{ .Values.imagelocation.domainname }}/{{ .Values.imagelocation.project }}/"+image;
    }
}
