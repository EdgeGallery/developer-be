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

package org.edgegallery.developer.model.application;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;
import javax.validation.constraints.Pattern;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;

@Getter
@Setter
@ToString
public class Application {

    private String id;

    private String name;

    @Pattern(regexp = "^(?!\\s)[\\S.\\s\\n\\r]{1,128}$")
    private String description;

    @Pattern(regexp = "^[\\w\\-][\\w\\-\\s.]{0,9}$")
    private String version;

    @Pattern(regexp = "^\\S.{0,29}$")
    private String provider;

    private String architecture;

    // appClass can be CONTAINER/VM
    private EnumAppClass appClass;

    private String type;

    private String industry;

    private String iconFileId;

    //appCreateType can be INTEGRATE/DEVELOP
    private EnumApplicationType appCreateType;

    private Date createTime;

    private EnumApplicationStatus status;

    private String userId;

    private String userName;

    private AppConfiguration appConfiguration;

    public String getId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        return this.id;
    }

    /**
     * initialProject.
     */
    public void initialProject() {
        this.status = EnumApplicationStatus.ONLINE;
    }

}
