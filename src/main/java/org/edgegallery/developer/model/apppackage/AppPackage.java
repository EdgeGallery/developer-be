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

package org.edgegallery.developer.model.apppackage;

import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;

@ToString
@Getter
@Setter
public class AppPackage {

    private String id;

    private String appId;

    private String packageFileName;

    /**
     * get package path.
     *
     * @return package path
     */
    public String getPkgPath() {
        return InitConfigUtil.getWorkSpaceBaseDir().concat(BusinessConfigUtil.getWorkspacePath()).concat(appId)
            .concat(File.separator).concat(packageFileName);
    }
}
