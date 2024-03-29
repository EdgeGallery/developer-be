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
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.mapper.application.AppScriptMapper;
import org.edgegallery.developer.model.application.Script;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.AppScriptService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.util.filechecker.ScriptChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("appScriptService")
public class AppScriptServiceImpl implements AppScriptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppScriptServiceImpl.class);

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private AppScriptMapper appScriptMapper;

    @Override
    public Script uploadScriptFile(String applicationId, MultipartFile scriptFile) {
        new ScriptChecker().check(scriptFile);
        UploadFile uploadFile = uploadFileService
            .uploadFile(AccessUserUtil.getUser().getUserId(), Consts.FILE_TYPE_SCRIPT, scriptFile);
        Script script = new Script();
        script.setId(UUID.randomUUID().toString());
        script.setName(uploadFile.getFileName());
        script.setScriptFileId(uploadFile.getFileId());
        script.setCreateTime(new Date());
        appScriptMapper.createAppScript(applicationId, script);
        LOGGER.info("upload script successfully.");
        return script;
    }

    @Override
    public List<Script> getScriptsByAppId(String applicationId) {
        return appScriptMapper.getScriptsByAppId(applicationId);
    }
}
