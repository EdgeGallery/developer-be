package org.edgegallery.developer.model.restful;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
@Getter
@Setter
public class VMImageReq {
    @Length(max = 128)
    private String name;

    @ApiModelProperty(example = "public")
    @Pattern(regexp = "All|public|private")
    private String visibleType;

    @Length(max = 50)
    private String userId;

    @Length(max = 50)
    private String osType;

    @ApiModelProperty(example = "UPLOAD_WAIT")
    @Pattern(regexp = "All|UPLOAD_WAIT|UPLOADING|UPLOAD_SUCCEED|UPLOAD_FAILED|PUBLISHED")
    private String status;

    private String uploadTimeBegin;

    private String uploadTimeEnd;

    @NotBlank
    @Valid
    private VMImageQuery queryCtrl;

}
