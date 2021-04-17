/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.model.workspace;

import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationProject {

    private static final String NAME_MSG = "请输入不含空格的4-32个字符的项目名，只能包含数字、字母、_、-，开头、结尾不能为_、-";

    private static final String VERSION_MSG = "请输入1-10个字符的版本号，只能包含数字、字母、_、-、空格，开头不能为空";

    private static final String PROVIDER_MSG = "请输入1-30个字符的提供者名称，开头不能为空";

    private static final String DESCRIPTION_MSG = "请输入1-128个字符的描述，开头不能为空";

    // normal data start
    private String id;

    private EnumProjectType projectType;

    @Pattern(regexp = "^(?!_)(?!-)(?!\\s)(?!.*?_$)(?!.*?-$)(?!.*?\\s$)[a-zA-Z0-9_-]{4,32}$", message = NAME_MSG)
    private String name;

    @Pattern(regexp = "^[\\w\\-][\\w\\-\\s.]{0,9}$", message = VERSION_MSG)
    private String version;

    @Pattern(regexp = "^\\S.{0,29}$", message = PROVIDER_MSG)
    private String provider;

    private List<String> platform;

    /**
     * the platform where deploy.
     */
    private EnumDeployPlatform deployPlatform;

    // add to match app store
    private String type;

    private List<String> industry;

    @Pattern(regexp = "^(?!\\s)[\\S.\\s\\n\\r]{1,128}$", message = DESCRIPTION_MSG)
    private String description;

    private String iconFileId;

    // Online or Deploying or Deployed or Testing or Tested
    private EnumProjectStatus status;

    private List<OpenMepCapabilityGroup> capabilityList;

    private String lastTestId;

    private String userId;

    private String createDate;

    private String openCapabilityId;

    /**
     * getId.
     */
    public String getId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        return this.id;
    }

    public void setIconFileId(String iconFileId) {
        this.iconFileId = iconFileId;
    }

    /**
     * initialProject.
     */
    public void initialProject() {
        this.lastTestId = null;
    }

}
