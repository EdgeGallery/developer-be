package org.edgegallery.developer.model.lcm;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributeResponse {

    private String appPkgName;

    private String appPkgVersion;

    private String appProvider;

    private String appPkgDesc;

    private String appPkgAffinity;

    private String appId;

    private String packageId;

    private String appIconUrl;

    private String createdTime;

    private String modifiedTime;

    private List<MecHostInfo> mecHostInfo;

}
