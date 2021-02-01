package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployYaml {
    private String apiVersion;
    private String kind;
    private MetaData metaData;
    private Spec spec;

}
