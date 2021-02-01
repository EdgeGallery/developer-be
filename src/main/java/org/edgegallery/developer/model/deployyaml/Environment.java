package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Environment {
    private String name;

    private String value;

    private ValueFrom valueFrom;

}
