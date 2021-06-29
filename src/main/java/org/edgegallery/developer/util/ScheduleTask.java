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

package org.edgegallery.developer.util;

import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.UploadFileService;
import org.edgegallery.developer.service.virtual.VmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class ScheduleTask {

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private VmService vmService;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void deleteTempFile() {
        uploadFileService.deleteTempFile();
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void processConfigDeploy() {
        projectService.processDeploy();
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void processVmCreateConfig() {
        vmService.processCreateVm();
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void processVmImageConfig() {
        vmService.processVmImage();
    }

    @Scheduled(cron = "0 0 22 * * ? ")
    public void processCleanEnv() {
        projectService.cleanUnreleasedEnv();
    }
}
