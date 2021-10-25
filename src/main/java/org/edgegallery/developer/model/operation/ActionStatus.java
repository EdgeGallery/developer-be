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

package org.edgegallery.developer.model.operation;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ActionStatus {

    private String id;

    private EnumOperationObjectType objectType;

    private String objectId;

    private String actionName;

    //progress should be 0-100
    private int progress;

    private EnumActionStatus status;

    private String errorMsg;

    private String statusLog;

    private String updateTime;

    public void appendStatusLog(String log) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        if (statusLog == null) {
            statusLog = "";
        }
        this.statusLog = statusLog + sdf.format(date) + "： " + log + System.getProperty("line.separator");
    }

}
