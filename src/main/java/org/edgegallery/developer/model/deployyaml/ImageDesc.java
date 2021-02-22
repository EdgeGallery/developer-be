package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDesc {

    private String id;

    private String name;

    private String version;

    private String checksum;

    private String containerFormat;

    private String diskFormat;

    private int minDisk;

    private int minRam;

    private String architecture;

    private int size;

    private String swImage;

    private String hw_scsi_model;

    private String hw_disk_bus;

    private String operatingSystem;

    private String supportedVirtualisationEnvironment;

}
