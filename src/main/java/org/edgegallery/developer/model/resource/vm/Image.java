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

package org.edgegallery.developer.model.resource.vm;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.workspace.EnumSystemImageStatus;

@Getter
@Setter
@ToString
public class Image {

    private String id;

    private String name;

    private String visibleType;

    private String osType;

    private String osVersion;

    private int systemDiskSize;

    private String imgFileName;

    private String imageFormat;

    private String  downLoadUrl;

    private String fileMd5;

    private String status;

    private Date createTime;

    private Date modifyTime;

    private Date uploadTime;

    private String userId;

    private String userName;
}
