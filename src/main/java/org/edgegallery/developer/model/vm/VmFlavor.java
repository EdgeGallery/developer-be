package org.edgegallery.developer.model.vm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmFlavor {
    private String architecture;

    private String flavor;

    private String constraints;

}
