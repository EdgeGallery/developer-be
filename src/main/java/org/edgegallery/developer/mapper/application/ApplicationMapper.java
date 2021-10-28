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
package org.edgegallery.developer.mapper.application;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.application.Application;

@Mapper
public interface ApplicationMapper {

    int createApplication(Application application);

    int modifyApplication(Application application);

    int deleteApplication(String id);

    List<Application> getAllApplications();

    List<Application> getAllApplicationsByUserId(@Param("userId")String userId,@Param("name") String name);

    Application getApplicationById(String id);

    int modifyMepHostById(@Param("applicationId")String applicationId, @Param("hostMepId")String hostMepId);

    int updateApplicationStatus(@Param("applicationId")String applicationId, @Param("status")String status);
}
