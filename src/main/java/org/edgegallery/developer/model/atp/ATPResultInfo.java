package org.edgegallery.developer.model.atp;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ATPResultInfo {
    String id;

    String appName;

    String status;

    Date createTime;
}
