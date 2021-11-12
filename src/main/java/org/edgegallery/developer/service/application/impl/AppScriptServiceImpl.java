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

package org.edgegallery.developer.service.application.impl;

import java.util.Date;
import java.util.UUID;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.ScriptChecker;
import org.edgegallery.developer.mapper.application.AppScriptMapper;
import org.edgegallery.developer.model.application.Script;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.AppScriptService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("AppScriptService")
public class AppScriptServiceImpl implements AppScriptService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppScriptServiceImpl.class);

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private AppScriptMapper appScriptMapper;

    @Override
    public Script uploadScriptFile(String applicationId, MultipartFile scriptFile) {
        new ScriptChecker().check(scriptFile);
        UploadFile uploadFile = uploadFileService.uploadFile(AccessUserUtil.getUser().getUserId(), "sh", scriptFile);
        Script script = new Script(UUID.randomUUID().toString(), uploadFile.getFileName(), uploadFile.getFileId(),
            new Date());
        appScriptMapper.createAppScript(applicationId, script);
        LOGGER.info("upload script successfully.");
        return script;
    }
}
