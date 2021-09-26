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
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.workspace.EnumSystemImageSlimStatus;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class VmSystem {

    private Integer systemId;

    @ApiModelProperty(example = "public")
    @Pattern(regexp = "All|public|private")
    private String type;

    @ApiModelProperty(example = "ubuntu")
    @Pattern(regexp = "ubuntu|centos|windows|cirros")
    private String operateSystem;

    @Length(max = 50)
    private String version;

    @Length(max = 50)
    private String systemBit;

    @Range(min = 10, max = 9999)
    private Integer systemDisk;

    @Length(max = 128)
    private String systemName;

    private String fileName;

    private String createTime;

    private String modifyTime;

    @Length(max = 50)
    private String systemFormat;

    private Integer systemSize;

    private EnumSystemImageSlimStatus systemSlim;

    private String uploadTime;

    @Length(max = 128)
    private String systemPath;

    private EnumSystemImageStatus status;

    @Length(max = 50)
    private String userId;

    @Length(max = 50)
    private String userName;

    private String fileMd5;

    private String fileIdentifier;

    private String errorType;

}
