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
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.atp.AtpTest;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.restful.SelectMepHostReq;

public interface AppOperationService {

    /**
     * clean application test environment.
     *
     * @param applicationId application id
     * @param user current login user
     * @return
     */
    Boolean cleanEnv(String applicationId, User user);

    /**
     * when deploy and test application, generate package.
     *
     * @param applicationId application id
     * @return
     */
    AppPackage generatePackage(String applicationId);

    /**
     * create test task.
     *
     * @param applicationId application id
     * @param user current login user
     * @return
     */
    Boolean createAtpTest(String applicationId, User user);

    /**
     * when deploy and test application, select test sandbox.
     *
     * @param applicationId application id
     * @param selectMepHostReq selected sandbox
     * @return
     */
    Boolean selectMepHost(String applicationId, SelectMepHostReq selectMepHostReq);

    /**
     * get created test tasks.
     *
     * @param applicationId application id
     * @return
     */
    List<AtpTest> getAtpTests(String applicationId);

    /**
     * get test task by id.
     *
     * @param atpTestId test id
     * @return
     */
    AtpTest getAtpTestById(String atpTestId);

    /**
     * release application to app store.
     *
     * @param applicationId application id
     * @param user current login user
     * @param publishAppDto release condition
     * @return
     */
    Boolean releaseApp(String applicationId, User user, PublishAppReqDto publishAppDto);

}
