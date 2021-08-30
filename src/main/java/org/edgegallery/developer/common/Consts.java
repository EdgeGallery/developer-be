/*
 * Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.common;

public final class Consts {

    public static final String DOWNLOAD_FILE_URL_V1 = "/mec/developer/v1/files/";

    public static final String APP_LCM_INSTANTIATE_APP_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/instantiate";

    public static final String APP_LCM_UPLOAD_APPPKG_URL = "/lcmcontroller/v2/tenants/tenantId/packages";

    public static final String APP_LCM_DISTRIBUTE_APPPKG_URL = "/lcmcontroller/v2/tenants/tenantId/packages/packageId";

    public static final String APP_LCM_DELETE_HOST_URL
        = "/lcmcontroller/v1/tenants/tenantId/packages/packageId/hosts/hostIp";

    public static final String APP_LCM_DELETE_APPPKG_URL = "/lcmcontroller/v2/tenants/tenantId/packages/packageId";

    public static final String APP_LCM_INSTANTIATE_IMAGE_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/images";

    public static final String APP_LCM_TERMINATE_APP_URL
        = "/lcmcontroller/v2/tenants/tenantId/app_instances/appInstanceId/terminate";

    public static final String APP_LCM_GET_WORKLOAD_STATUS_URL
        = "/lcmcontroller/v2/tenants/tenantId/app_instances/appInstanceId";

    public static final String APP_LCM_GET_WORKLOAD_EVENTS_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/workload/events";

    public static final String APP_LCM_GET_IMAGE_STATUS_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/images/imageId";

    public static final String APP_LCM_GET_IMAGE_DELETE_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/images/imageId";

    public static final String APP_LCM_GET_IMAGE_DOWNLOAD_URL
        = "/lcmcontroller/v1/tenants/tenantId/app_instances/appInstanceId/images/imageId/file";

    public static final String APP_LCM_GET_HEALTH = "/lcmcontroller/v1/health";

    public static final String APP_LCM_ADD_MECHOST = "/lcmcontroller/v1/hosts";

    public static final String APP_LCM_UPLOAD_FILE = "/lcmcontroller/v1/configuration";

    public static final String APP_LCM_GET_DEPLOY_STATUS_URL = "/lcmcontroller/v1/hosts/hostIp/packages/packageId";

    public static final long MINUTE = 60000;

    public static final int TEMP_FILE_TIMEOUT = 30;

    public static final String FILE_ENCODING = "utf-8";

    public static final String ACCESS_TOKEN_STR = "access_token";

    public static final String ROLE_DEVELOPER_ADMIN = "ROLE_DEVELOPER_ADMIN";

    public static final String CHUNK_NUM = "chunk_num";

    public static final String RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR = "Internal exception.";

    public static final String RESPONSE_MESSAGE_CAN_NOT_FIND_PROJECT = "Can not find project.";

    public static final String PATTERN
        = "[\\u4e00-\\u9fa5 `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~_！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";

    public static final int QUERY_APPLICATIONS_TIMES = 20;

    public static final int QUERY_APPLICATIONS_PERIOD = 30000;

    public static final String CREATE_TASK_FROM_ATP = "/edgegallery/atp/v1/tasks";

    public static final String GET_TASK_FROM_ATP = "/edgegallery/atp/v1/tasks/%s";

    public static final String DEV_CLEAN_ENV_URL = "%s/mec/developer/v1/projects/%s/action/clean?userId=%s";

    public static final String SYSTEM_IMAGE_UPLOAD_URL = "/image-management/v1/images";

    public static final String SYSTEM_IMAGE_DOWNLOAD_URL = "/image-management/v1/images/%s/action/download";

    public static final String SYSTEM_IMAGE_GET_URL = "/image-management/v1/images/%s";

    public static final String SYSTEM_IMAGE_SLICE_UPLOAD_URL = "/image-management/v1/images/upload";

    public static final String SYSTEM_IMAGE_SLICE_MERGE_URL = "/image-management/v1/images/merge";

    public static final String HARBOR_IMAGE_LOGIN_URL = "%s://%s/c/login";

    public static final String HARBOR_IMAGE_DELETE_URL = "%s://%s/api/v2.0/projects/%s/repositories/%s/artifacts/%s";

    public static final String HARBOR_IMAGE_CREATE_REPO_URL = "%s://%s/api/v2.0/projects";

    public static final String HARBOR_IMAGE_GET_LIST_URL
        = "%s://%s/api/v2.0/projects/%s/repositories/?page=1&page_size=1000";

    public static final String HARBOR_IMAGE_GET_TAGS_URL = "%s://%s/api/v2.0/projects/%s/repositories/%s/artifacts";

    public static final String HARBOR_PRO_IS_EXIST_URL = "%s://%s/api/v2.0/projects?name=%s";

    private Consts() {
    }
}
