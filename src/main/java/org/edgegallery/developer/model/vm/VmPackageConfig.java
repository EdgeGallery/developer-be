package org.edgegallery.developer.model.vm;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VmPackageConfig {

    private String id;

    private String projectId;

    private VmRegulation vmRegulation;

    private VmSystem vmSystem;

    private List<String> vmNetwork;

    private VmUserData vmUserData;

    private String vmName;

    private String ak;

    private String sk;

    private String appInstanceId;

    private Date createTime;

    /**
     * getCreateTime.
     *
     * @return
     */
    public Date getCreateTime() {
        if (createTime != null) {
            return (Date) createTime.clone();
        }
        return null;
    }

    /**
     * setCreateTime.
     *
     * @param createTime createTime
     */
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = (Date) createTime.clone();
        } else {
            this.createTime = null;
        }

    }

}
