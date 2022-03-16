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
package org.edgegallery.developer.model.instantiate.vm;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImageExportInfo {

    private String operationId;

    private String imageInstanceId;

    private String name;

    private String imageFileName;

    private String format;

    private String checkSum;

    private EnumImageExportStatus status;

    private String downloadUrl;

    private String imageSize;

    private Date createTime;

    private String log;

    /**
     * get create time.
     */
    public Date getCreateTime() {
        return this.createTime != null ? new Date(this.createTime.getTime()) : null;
    }

    /**
     * set create time.
     */
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            this.createTime = (Date) createTime.clone();
        } else {
            this.createTime = null;
        }
    }

}
