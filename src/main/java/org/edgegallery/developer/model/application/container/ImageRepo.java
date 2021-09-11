package org.edgegallery.developer.model.application.container;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImageRepo {

    private String endpoint;

    private String project;

    private String name;

    private String version;

}
