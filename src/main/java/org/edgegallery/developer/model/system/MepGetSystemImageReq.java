/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.model.system;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class MepGetSystemImageReq {

    @Length(max = 128)
    private String systemName;

    @ApiModelProperty(example = "public")
    @Pattern(regexp = "All|public|private")
    private String type;

    @Length(max = 50)
    private String userId;

    @Length(max = 50)
    private String operateSystem;

    @ApiModelProperty(example = "UPLOAD_WAIT")
    @Pattern(regexp = "All|UPLOAD_WAIT|UPLOADING|UPLOAD_SUCCEED|UPLOAD_FAILED|PUBLISHED")
    private String status;

    private String createTimeBegin;

    private String createTimeEnd;

    @NotBlank
    @Valid
    private MepSystemQueryCtrl queryCtrl;

}
