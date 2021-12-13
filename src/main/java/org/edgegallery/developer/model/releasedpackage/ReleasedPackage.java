package org.edgegallery.developer.model.releasedpackage;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReleasedPackage {

    private String appId;

    private String packageId;

    private String name;

    private String version;

    private String provider;

    private String industry;

    private String type;

    private String architecture;

    private String shortDesc;

    private Date synchronizeDate;

    private String userId;

    private String userName;

    private String testTaskId;

}
