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
        = "/lcmcontroller/v2/tenants/%s/app_instances/%s/instantiate";

    public static final String APP_LCM_UPLOAD_APPPKG_URL = "/lcmcontroller/v2/tenants/%s/packages";

    public static final String APP_LCM_DISTRIBUTE_APPPKG_URL = "/lcmcontroller/v2/tenants/%s/packages/%s";

    public static final String APP_LCM_DELETE_HOST_URL
        = "/lcmcontroller/v2/tenants/%s/packages/%s/hosts/%s";

    public static final String APP_LCM_DELETE_APPPKG_URL = "/lcmcontroller/v2/tenants/%s/packages/%s";

    public static final String APP_LCM_INSTANTIATE_IMAGE_URL
        = "/rescontroller/v1/tenants/%s/hosts/%s/servers/%s";

    public static final String APP_LCM_GET_IMAGE_STATUS_URL
        = "/rescontroller/v1/tenants/%s/hosts/%s/images/%s";

    public static final String APP_LCM_TERMINATE_APP_URL
        = "/lcmcontroller/v2/tenants/%s/app_instances/%s/terminate";

    public static final String APP_LCM_GET_WORKLOAD_STATUS_URL
        = "/lcmcontroller/v2/tenants/%s/app_instances/%s";

    public static final String APP_LCM_GET_WORKLOAD_EVENTS_URL
        = "/lcmcontroller/v2/tenants/%s/app_instances/%s/workload/events";

    public static final String APP_LCM_GET_VNC_CONSOLE_URL
        = "/rescontroller/v1/tenants/%s/hosts/%s/servers/%s";

    public static final String APP_LCM_GET_HEALTH = "/lcmcontroller/v1/health";

    public static final String APP_LCM_ADD_MECHOST = "/lcmcontroller/v1/tenants/%s/hosts";

    public static final String APP_LCM_UPLOAD_FILE = "/lcmcontroller/v2/tenants/%s/configuration";

    public static final long MINUTE = 60000;

    public static final int TEMP_FILE_TIMEOUT = 30;

    public static final int UPLOAD_RETRANSPART_COUNT = 5;

    public static final String ACCESS_TOKEN_STR = "access_token";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String ROLE_DEVELOPER_ADMIN = "ROLE_DEVELOPER_ADMIN";

    public static final String PATTERN
        = "[\\u4e00-\\u9fa5 `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~_！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";

    public static final String CREATE_TASK_FROM_ATP = "/edgegallery/atp/v1/tasks";

    public static final String GET_TASK_FROM_ATP = "/edgegallery/atp/v1/tasks/%s";

    public static final String SYSTEM_IMAGE_DOWNLOAD_URL = "/image-management/v1/images/%s/action/download";

    public static final String SYSTEM_IMAGE_GET_URL = "/image-management/v1/images/%s";

    public static final String SYSTEM_IMAGE_SLICE_UPLOAD_URL = "/image-management/v1/images/upload";

    public static final String SYSTEM_IMAGE_SLICE_MERGE_URL = "/image-management/v1/images/merge";

    public static final String UPLOAD_TO_APPSTORE_URL = "/mec/appstore/v1/apps?userId=%s&userName=%s";

    public static final String PUBLISH_TO_APPSTORE_URL = "/mec/appstore/v1/apps/%s/packages/%s/action/publish";

    public static final String QUERY_APPSTORE_PKG_URL = "%s/mec/appstore/v2/apps/%s/packages/%s";

    public static final String DOWNLOAD_APPSTORE_PKG_URL = "%s/mec/appstore/v1/apps/%s/packages/%s/action/download";

    public static final String HARBOR_IMAGE_DELETE_URL = "%s://%s/api/v2.0/projects/%s/repositories/%s/artifacts/%s";

    public static final String HARBOR_IMAGE_CREATE_REPO_URL = "%s://%s/api/v2.0/projects";

    public static final String HARBOR_IMAGE_GET_LIST_URL
        = "%s://%s/api/v2.0/projects/%s/repositories/?page=1&page_size=1000";

    public static final String HARBOR_IMAGE_GET_TAGS_URL = "%s://%s/api/v2.0/projects/%s/repositories/%s/artifacts";

    public static final String HARBOR_PRO_IS_EXIST_URL = "%s://%s/api/v2.0/projects?name=%s";

    public static final int DEFAULT_OPENSTACK_VNC_PORT = 6080;

    public static final String FILE_FORMAT_MF = ".mf";

    public static final String FILE_FORMAT_ZIP = ".zip";

    public static final String FILE_FORMAT_YAML = ".yaml";

    public static final String FILE_FORMAT_CSAR = ".csar";

    public static final int LENGTH_64 = 64;

    public static final int LENGTH_255 = 255;

    public static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    public static final String FILE_TYPE_SCRIPT = "script";

    public static final String PROFILE_FILE_TYPE_PROFILE = "profileFile";

    public static final String PROFILE_FILE_TYPE_DEPLOY = "deployFile";

    public static final String PROFILE_FILE_TYPE_CONFIG = "configFile";

    private Consts() {
    }
}
