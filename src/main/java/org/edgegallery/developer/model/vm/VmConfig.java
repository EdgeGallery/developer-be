package org.edgegallery.developer.model.vm;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfigStageStatus;
import org.stringtemplate.v4.ST;

@Getter
@Setter
@ToString
public class VmConfig {

    private String vmId;

    private String projectId;

    private VmRegulation vmRegulationDesc;

    private VmSystem vmSystemDesc;

    private VmNetwork vmNetworkDesc;

    private String vmName;

    private String vmUsername;

    private EnumVmCreateStatus status;

    private VmCreateStageStatus stageStatus;

    private MepHost host;

    private String lcmToken;

    private String vmInfo;

    private String packageId;

    private Date create_time;

    private String log;

    public VmConfig () {}
}
