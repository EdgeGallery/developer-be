package org.edgegallery.developer.model.vm;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmImageConfig {

    private String VmId;

    private String imageId;

    private String projectId;

    private String vmName;

    private String imageName;

    private String appInstanceId;

    private String hostIp;

    private Integer sumChunkNum;

    private Integer chunkSize;

    private VmImportStageStatus stageStatus;

    private EnumVmImportStatus status;

    private String lcmToken;

    private Date createTime;

    private String log;

    public VmImageConfig() {}

    /**
     * get next stage for deploy.
     */
    public String getNextStage() {
        if (this.getStageStatus() == null || this.getStageStatus().getCreateImageInfo() == null) {
            return "createImageInfo";
        } else if (this.getStageStatus().getImageStatus() == null) {
            return "imageStatus";
        } else if (this.getStageStatus().getDownloadImageInfo() == null) {
            return "downloadImageInfo";
        }
        return null;
    }






}
