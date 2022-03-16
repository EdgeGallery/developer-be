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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;

@Getter
@Setter
@ToString
public class VMInstantiateInfo {

    private String operationId;

    private String appPackageId;

    private String distributedMecHost;

    private String mepmPackageId;

    private String appInstanceId;

    private String vmInstanceId;

    private EnumAppInstantiateStatus status;

    private Date instantiateTime;

    private String log;

    private String vncUrl;

    private List<PortInstantiateInfo> portInstanceList = new ArrayList<>(0);

    /**
     * get instantiateTime.
     */
    public Date getInstantiateTime() {
        return this.instantiateTime != null ? new Date(this.instantiateTime.getTime()) : null;
    }

    /**
     * set instantiateTime.
     */
    public void setInstantiateTime(Date instantiateTime) {
        if (instantiateTime != null) {
            this.instantiateTime = (Date) instantiateTime.clone();
        } else {
            this.instantiateTime = null;
        }
    }


}
