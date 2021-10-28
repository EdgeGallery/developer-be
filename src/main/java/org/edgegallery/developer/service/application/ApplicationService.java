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

import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.springframework.transaction.annotation.Transactional;

public interface ApplicationService {

    /**
     * create a application
     *
     * @param application application
     * @return
     */
    @Transactional
    public Application createApplication(Application application);

    /**
     * get a application
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    public Application getApplication(String applicationId);

    /**
     * modify a application
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean modifyApplication(String applicationId, Application application);

    /**
     * get a application
     *
     * @return
     */
    @Transactional
    Page<Application> getApplicationByNameWithFuzzy(String appName, int limit, int offset);

    /**
     * DELETE a application
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Boolean deleteApplication(String applicationId);

    ApplicationDetail getApplicationDetail(String applicationId);

    Boolean modifyApplicationDetail(String applicationId, ApplicationDetail applicationDetail);

}
