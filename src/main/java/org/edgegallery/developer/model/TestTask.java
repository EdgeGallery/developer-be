/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestTask {

    private String taskId;

    private String taskNo;

    private String status;

    private Date beginTime;

    private Date endTime;

    private String appId;

    /**
     * getBeginTime.
     */
    public Date getBeginTime() {
        if (this.beginTime != null) {
            return new Date(this.beginTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setBeginTime.
     */
    public void setBeginTime(Date beginTime) {
        if (beginTime != null) {
            this.beginTime = (Date) beginTime.clone();
        } else {
            this.beginTime = null;
        }
    }

    /**
     * getEndTime.
     */
    public Date getEndTime() {
        if (this.endTime != null) {
            return new Date(this.endTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setEndTime.
     */
    public void setEndTime(Date endTime) {
        if (endTime != null) {
            this.endTime = (Date) endTime.clone();
        } else {
            this.endTime = null;
        }
    }
}
