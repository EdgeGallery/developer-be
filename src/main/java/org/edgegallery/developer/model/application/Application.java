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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.atp.AtpTest;

@Getter
@Setter
@ToString
public class Application {

    public static final String APPCLASS_CONTAINER = "CONTAINER";

    public static final String APPCLASS_VM = "VM";

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

    private String guideFileId;

    //appCreateType can be INTEGRATE/DEVELOP
    private EnumApplicationType appCreateType;

    private String createTime;

    private EnumApplicationStatus status;

    private String userId;

    private String userName;

    private String mepHostId;

    private String pkgSpecId;

    private AppPackage appPackage = new AppPackage();

    private List<AtpTest> atpTestTaskList = new ArrayList<>(0);

    private AppConfiguration appConfiguration = new AppConfiguration();

    private List<Script> scriptList = new ArrayList<>(0);

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
        this.status = EnumApplicationStatus.CONFIGURED;
    }

    public Application() {

    }

    public Application(Application app) {
        this.id = app.id;
        this.name = app.name;
        this.description = app.description;
        this.version = app.version;
        this.provider = app.provider;
        this.architecture = app.architecture;
        this.appClass = app.appClass;
        this.type = app.type;
        this.industry = app.industry;
        this.iconFileId = app.iconFileId;
        this.guideFileId = app.guideFileId;
        this.appCreateType = app.appCreateType;
        this.createTime = app.createTime;
        this.status = app.status;
        this.userId = app.userId;
        this.userName = app.userName;
        this.mepHostId = app.mepHostId;
        this.pkgSpecId = app.pkgSpecId;
        this.appConfiguration = app.appConfiguration;
        this.appPackage = app.appPackage;
        this.atpTestTaskList = app.atpTestTaskList;
        this.scriptList = app.scriptList;
    }

}
