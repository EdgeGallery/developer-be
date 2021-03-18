package org.edgegallery.developer.model.vm;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.workspace.MepHost;

@Getter
@Setter
@ToString
public class VmCreateConfig {

    private String vmId;

    private String projectId;

    private VmRegulation vmRegulation;

    private VmSystem vmSystem;

    private List<String> vmNetwork;

    private String vmName;

    private EnumVmCreateStatus status;

    private VmCreateStageStatus stageStatus;

    private MepHost host;

    private String lcmToken;

    private List<VmInfo> vmInfo;

    private String appInstanceId;

    private String packageId;

    private Date createTime;

    private String log;

    public VmCreateConfig() {
    }

    /**
     * get next stage for deploy.
     */
    public String getNextStage() {
        if (this.getStageStatus() == null || this.getStageStatus().getCsar() == null) {
            return "hostInfo";
        } else if (this.getStageStatus().getHostInfo() == null) {
            return "csar";
        } else if (this.getStageStatus().getInstantiateInfo() == null) {
            return "instantiateInfo";
        } else if (this.getStageStatus().getWorkStatus() == null) {
            return "workStatus";
        }
        return null;
    }

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
