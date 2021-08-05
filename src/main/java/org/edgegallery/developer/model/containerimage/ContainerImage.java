package org.edgegallery.developer.model.containerimage;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerImage {

    private String imageId;

    private String imageName;

    private String imageVersion;

    private String userId;

    private String userName;

    private Date uploadTime;

    private Date createTime;

    //not upload,uoloaded,uploading,cancel upload
    private EnumContainerImageStatus imageStatus;

    //public private
    private String imageType;

    private String imagePath;

    private String fileName;

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

    /**
     * getUploadTime.
     */
    public Date getUploadTime() {
        if (this.uploadTime != null) {
            return new Date(this.uploadTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setUploadTime.
     */
    public void setUploadTime(Date uploadTime) {
        if (uploadTime != null) {
            this.uploadTime = (Date) uploadTime.clone();
        } else {
            this.uploadTime = null;
        }
    }

}
