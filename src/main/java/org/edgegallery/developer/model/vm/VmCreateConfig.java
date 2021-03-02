package org.edgegallery.developer.model.vm;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfigStageStatus;
import org.stringtemplate.v4.ST;

@Getter
@Setter
@ToString
public class VmCreateConfig {

    private String vmId;

    private String projectId;

    private VmRegulation vmRegulationDesc;

    private VmSystem vmSystemDesc;

    private List<String> vmNetworkDesc;

    private String vmName;

    private EnumVmCreateStatus status;

    private VmCreateStageStatus stageStatus;

    private MepHost host;

    private String lcmToken;

    private List<VmInfo> vmInfo;

    private String appInstanceId;

    private Date createTime;

    private String log;

    public VmCreateConfig() {}
    /**
     * get next stage for deploy.
     */
    public String getNextStage() {
        if (this.getStageStatus() == null || this.getStageStatus().getCsar() == null) {
            return "csar";
        } else if (this.getStageStatus().getHostInfo() == null) {
            return "hostInfo";
        } else if (this.getStageStatus().getInstantiateInfo() == null) {
            return "instantiateInfo";
        } else if (this.getStageStatus().getWorkStatus() == null) {
            return "workStatus";
        }
        return null;
    }
}
