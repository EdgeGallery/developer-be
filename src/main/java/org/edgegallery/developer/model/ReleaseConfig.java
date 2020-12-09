package org.edgegallery.developer.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.atp.ATPResultInfo;

/**
 * @author chenhui
 */
@Setter
@Getter
public class ReleaseConfig {
    private String releaseId;

    private String projectId;

    private String guideFileId;

    private String appInstanceId;

    private CapabilitiesDetail capabilitiesDetail;

    private ATPResultInfo atpTest;

    private String testStatus;

    private Date createTime;

    /**
     * getCreateTime.
     */
    public Date getCreateTime() {
        if (this.createTime != null) {
            return new Date(this.createTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setCreateTime.
     */
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = (Date) createTime.clone();
        } else {
            this.createTime = null;
        }
    }
}
