package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaData {
    private String name;
    private String namespace;
    private Labels labels;
}
