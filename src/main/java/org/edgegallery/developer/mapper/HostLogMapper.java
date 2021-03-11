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

package org.edgegallery.developer.mapper;


import org.apache.ibatis.annotations.*;
import org.edgegallery.developer.model.workspace.MepHostLog;

import java.util.List;

public interface HostLogMapper {

    @Select("select * from tbl_host_log where host_id = #{hostId}")
    @Results(id = "hostLog", value = {
            @Result(property = "logId", column = "log_id"),
            @Result(property = "hostIp", column = "host_ip"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "projectId", column = "project_id"),
            @Result(property = "projectName", column = "project_name"),
            @Result(property = "appInstancesId", column = "app_instances_id"),
            @Result(property = "deployTime", column = "deploy_time"),
            @Result(property = "hostId", column = "host_id"),
    })
    @ResultType(MepHostLog.class)
    List<MepHostLog> getHostLogByHostId(@Param("hostId") String hostId);

    @Insert("insert into tbl_host_log(log_id,host_ip,user_name,user_id,project_id,project_name,app_instances_id,deploy_time,host_id,status,operation)" +
            "values (#{logId} ,#{hostIp} ,#{userName} ,#{userId} ,#{projectId} ,#{projectName} ,#{appInstancesId} ,#{deployTime} ,#{hostId} ,#{status} ,#{operation} )")
    int insert(MepHostLog hostLog);
}

