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

package org.edgegallery.developer.model.instantiate.container;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ContainerAppInstantiateInfo {

    private String appPackageId;

    private String appInstanceId;

    private EnumContainerAppInstantiateStatus status;

    private String log;

    private Date instantiateTime;

    private List<K8sPod> pods;

    private List<K8sService> serviceList;

    /**
     * getInstantiateTime.
     *
     * @return
     */
    public Date getInstantiateTime() {
        if (this.instantiateTime != null) {
            return new Date(this.instantiateTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setInstantiateTime.
     *
     * @param instantiateTime instantiateTime
     */
    public void setInstantiateTime(Date instantiateTime) {
        if (instantiateTime != null) {
            this.instantiateTime = (Date) instantiateTime.clone();
        } else {
            this.instantiateTime = null;
        }
    }

}
