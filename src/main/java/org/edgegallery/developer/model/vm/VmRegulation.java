package org.edgegallery.developer.model.vm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VmRegulation {

    private String regulationId;

    private String architecture;

    private String name;

    private String description;

    private String memory;

    private String cpu;

    private String systemDisk;

    private String virtualDisk;

    private String gpu;

    private String otherAbility;

    public VmRegulation (){}

}
