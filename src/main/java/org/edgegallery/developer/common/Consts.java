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

package org.edgegallery.developer.common;

public final class Consts {

    public static final String DOWNLOAD_FILE_URL_V1 = "/mec/developer/v1/files/";

    public static final String APP_LCM_INSTANTIATE_APP_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/instantiate";

    public static final String APP_LCM_TERMINATE_APP_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/terminate";

    public static final String APP_LCM_GET_WORKLOAD_STATUS_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId";

    public static final long MINUTE = 60000;

    public static final int TEMP_FILE_TIMEOUT = 30;

    public static final String FILE_ENCODING = "utf-8";

    public static final String ACCESS_TOKEN_STR = "access_token";

    public static final String RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR = "Internal exception.";

    public static final String RESPONSE_MESSAGE_CAN_NOT_FIND_PROJECT = "Can not find project.";

    public static final String PATTERN = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~_！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";

    public static final int QUERY_APPLICATIONS_TIMES = 8;

    public static final int QUERY_APPLICATIONS_PERIOD = 10000;

    private Consts() {
    }
}
