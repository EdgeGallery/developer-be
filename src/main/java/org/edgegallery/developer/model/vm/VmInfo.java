package org.edgegallery.developer.model.vm;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmInfo {

    private String vmId;

    private String vncUrl;

    private List<NetworkInfo> networks;

}
