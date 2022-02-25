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

import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.restful.ApplicationDetail;

public interface ApplicationService {

    /**
     * create a application.
     *
     * @param application application
     * @return
     */
    Application createApplication(Application application);

    /**
     * get a application.
     *
     * @param applicationId applicationId
     * @return
     */
    Application getApplication(String applicationId);

    /**
     * modify a application.
     *
     * @param applicationId applicationId
     * @return
     */
    Boolean modifyApplication(String applicationId, Application application);

    /**
     * get a application with name.
     *
     * @param appName application name
     * @param limit page limit
     * @param offset page offset
     * @return
     */
    Page<Application> getApplicationByNameWithFuzzy(String appName, int limit, int offset);

    /**
     * delete a application.
     *
     * @param applicationId applicationId
     * @param user delete app author
     * @return
     */
    Boolean deleteApplication(String applicationId, User user);

    /**
     * get application detail.
     *
     * @param applicationId applicationId
     * @return
     */
    ApplicationDetail getApplicationDetail(String applicationId);

    /**
     * modify application detail.
     *
     * @param applicationId applicationId
     * @param applicationDetail applicationDetail
     * @return
     */
    Boolean modifyApplicationDetail(String applicationId, ApplicationDetail applicationDetail);

    /**
     * update application status.
     *
     * @param applicationId applicationId
     * @param status application status
     * @return
     */
    Boolean updateApplicationStatus(String applicationId, EnumApplicationStatus status);

    /**
     * modify host id in one application.
     *
     * @param applicationId applicationId
     * @param mepHostId host id
     * @return
     */
    Boolean modifyMepHostById(String applicationId, String mepHostId);

}
