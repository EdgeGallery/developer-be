package org.edgegallery.developer.model.releasedpackage;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.apppackage.AppPackage;

@Getter
@Setter
public class ReleasedPackage {

    private String id;

    private String appStoreAppId;

    private String appStorePackageId;

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

     private AppPackage appPackage;

}
