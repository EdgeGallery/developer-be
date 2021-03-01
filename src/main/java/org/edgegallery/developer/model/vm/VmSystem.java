package org.edgegallery.developer.model.vm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VmSystem {

    private Integer systemId;

    private String type;

    private String operateSystem;

    private String version;

    private String systemBit;

    private Integer systemDisk;

}
