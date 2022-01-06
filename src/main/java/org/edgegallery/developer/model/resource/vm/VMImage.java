/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.model.resource.vm;

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
    @Pattern(regexp = "ubuntu|centos|windows|cirros")
    private String osType;

    @Length(max = 50)
    private String osVersion;

    @Length(max = 50)
    private String osBitType;

    @Range(min = 1, max = 9999)
    private Integer systemDiskSize;

    @ApiModelProperty(example = "virtio")
    @Pattern(regexp = "virtio|ide|scsi|")
    private String diskBus = "virtio";

    private float virtualSize;

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

    private Long imageSize;

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
