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
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;

import javax.validation.constraints.Pattern;
import java.util.Date;

@Getter
@Setter
public class VmSystem {

    private Integer systemId;

    private String type;

    private String operateSystem;

    private String version;

    private String systemBit;

    private Integer systemDisk;

    private String systemName;

    private Date createTime;

    private Date modifyTime;

    private String systemFormat;

    private Date uploadTime;

    private String systemPath;

    private EnumSystemImageStatus status;

    private String userId;

    private String userName;

}
