package org.edgegallery.developer.model.vmimage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
@Getter
@Setter
public class VMImage {

    private Integer id;

    @ApiModelProperty(example = "public")
    @Pattern(regexp = "All|public|private")
    private String visibleType;

    @ApiModelProperty(example = "ubuntu")
    @Pattern(regexp = "ubuntu|centos|window|cirros")
    private String osType;

    @Length(max = 50)
    private String osVersion;

    @Length(max = 50)
    private String osBitType;

    @Range(min = 10, max = 9999)
    private Integer systemDiskSize;

    @Length(max = 128)
    private String name;

    private String imageFileName;

    private String createTime;

    private String modifyTime;

    @Length(max = 50)
    private String imageFormat;

    private String uploadTime;

    @Length(max = 128)
    private String downLoadUrl;

    private String imageSize;

    private EnumVmImageStatus status;

    private EnumVmImageSlimStatus imageSlimStatus;

    @Length(max = 50)
    private String userId;

    @Length(max = 50)
    private String userName;

    private String fileMd5;

    private String fileIdentifier;

    private String errorType;

}
