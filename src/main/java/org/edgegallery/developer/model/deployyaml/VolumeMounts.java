package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VolumeMounts {
    private String name;
    private String mountPath;
    private String subPath;

}
