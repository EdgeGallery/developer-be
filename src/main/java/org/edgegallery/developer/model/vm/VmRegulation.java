package org.edgegallery.developer.model.vm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VmRegulation {

    private String architecture;

    private String nameZh;

    private String nameEn;

    private String sceneZh;

    private String sceneEn;

    private Integer memory;

    private Integer cpu;

    private Integer systemDisk;

    private Integer dataDisk;

    private String gpu;

    private String otherAbility;

    public VmRegulation (){}

}
