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

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TaskBean {

    private String taskid;

    private String appid;

    private String telnetid;

    private String status;

    private Timestamp createtime;

    /**
     * getCreatetime.
     */
    public Timestamp getCreatetime() {
        if (this.createtime != null) {
            return new Timestamp(this.createtime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setCreatetime.
     */
    public void setCreatetime(Timestamp createtime) {
        if (createtime != null) {
            this.createtime = (Timestamp) createtime.clone();
        } else {
            this.createtime = null;
        }
    }

}
