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

package org.edgegallery.developer.service.application;

import java.util.List;
import org.edgegallery.developer.model.application.Script;
import org.springframework.web.multipart.MultipartFile;

public interface AppScriptService {

    /**
     * upload app script file.
     *
     * @param applicationId application id
     * @param scriptFile script file
     * @return script info
     */
    Script uploadScriptFile(String applicationId, MultipartFile scriptFile);

    /**
     * get scripts by application id.
     *
     * @param applicationId application id
     * @return script list
     */
    List<Script> getScriptsByAppId(String applicationId);
}
