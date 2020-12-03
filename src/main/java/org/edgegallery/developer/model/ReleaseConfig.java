package org.edgegallery.developer.model;

import com.google.gson.Gson;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.atp.ATPResultInfo;

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

    // /**
    //  * setCapabilitiesDetail.
    //  */
    // public void setCapabilitiesDetail(CapabilitiesDetail capabilitiesDetail) {
    //     Gson gson = new Gson();
    //     this.capabilitiesDetail = gson.toJson(capabilitiesDetail);
    // }
    //
    // /**
    //  * setAtpTest.
    //  */
    // public void setAtpTest(ATPResultInfo atpTest) {
    //     Gson gson = new Gson();
    //     this.atpTest = gson.toJson(atpTest);
    // }
}
