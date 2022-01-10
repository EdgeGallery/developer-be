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

public final class ResponseConsts {

    private ResponseConsts() {
        throw new IllegalStateException("ResponseConsts class");
    }

    /**
     * request format is error.
     */
    public static final int RET_REQUEST_FORMAT_ERROR = 20001;

    /**
     * request param is error.
     */
    public static final int RET_REQUEST_PARAM_ERROR = 20002;

    /**
     * request param is empty.
     */
    public static final int RET_REQUEST_PARAM_EMPTY = 20003;

    /**
     * insert data  fail.
     */
    public static final int RET_CREATE_DATA_FAIL = 20004;

    /**
     * update data  fail.
     */
    public static final int RET_UPDATE_DATA_FAIL = 20005;

    /**
     * delete data  fail.
     */
    public static final int RET_DELETE_DATA_FAIL = 20006;

    /**
     * query data  empty.
     */
    public static final int RET_QUERY_DATA_EMPTY = 20007;

    /**
     * query data  fail.
     */
    public static final int RET_QUERY_DATA_FAIL = 20008;

    /**
     * sandbox health check fail.
     */
    public static final int RET_CREATE_SANDBOX_HEALTH_CHECK_FAIL = 20009;

    /**
     * push harbor Image  fail.
     */
    public static final int RET_PUSH_HARBOR_IMAGE_FAIL = 20010;

    /**
     * encode harbor user and pwd  fail.
     */
    public static final int RET_HARBOR_ENCODE_FAIL = 20011;

    /**
     * get harbor image list   fail.
     */
    public static final int RET_GET_HARBOR_IMAGE_LIST_FAIL = 20012;

    /**
     * query harbor project  fail.
     */
    public static final int RET_QUERY_HARBOR_PROJECT_FAIL = 20013;

    /**
     * request Unauthorized.
     */
    public static final int RET_REQUEST_UNAUTHORIZED = 20014;

    /**
     * request forbidden.
     */
    public static final int RET_REQUEST_FORBIDDEN = 20015;

    /**
     * file not found.
     */
    public static final int RET_FILE_NOT_FOUND = 20016;

    /**
     * Create file  fail.
     */
    public static final int RET_CREATE_FILE_FAIL = 20017;

    /**
     * upload file fail.
     */
    public static final int RET_UPLOAD_FILE_FAIL = 20018;

    /**
     * merge file fail.
     */
    public static final int RET_MERGE_FILE_FAIL = 20019;

    /**
     * delete file fail.
     */
    public static final int RET_DELETE_FILE_FAIL = 20020;

    /**
     * save file  fail.
     */
    public static final int RET_SAVE_FILE_FAIL = 20021;

    /**
     * download file  fail.
     */
    public static final int RET_DOWNLOAD_FILE_FAIL = 20022;

    /**
     * sandbox config network fail.
     */
    public static final int RET_CREATE_SANDBOX_CONFIG_NETWORK_FAIL = 20023;

    /**
     *  file format error.
     */
    public static final int RET_FILE_FORMAT_ERROR = 20024;

    /**
     *  image info doesn't exist in harbor.
     */
    public static final int RET_CHECK_IMAGE_EXIST_IN_HARBOR_FAIL= 20025;

    /**
     *  hash file fail.
     */
    public static final int RET_HASH_FILE_FAIL= 20026;

    /**
     *  image info doesn't exist in yaml.
     */
    public static final int RET_CHECK_IMAGE_EXIST_IN_YAML_FAIL= 20027;

    /**
     *  call lcm interce fail.
     */
    public static final int RET_CALL_LCM_FAIL= 20028;

    /**
     *  yaml load fail.
     */
    public static final int RET_LOAD_YAML_FAIL= 20029;

    /**
     *  decompress file fail.
     */
    public static final int RET_DECOMPRESS_FILE_FAIL= 20030;

    /**
     *  get file structure error.
     */
    public static final int RET_FILE_STRUCTURE_FAIL= 20031;

    /**
     *   file is empty.
     */
    public static final int RET_FILE_EMPTY= 20032;

    /**
     *   file not readable.
     */
    public static final int RET_FILE_NOT_READABLE= 20033;

    /**
     *  compress file fail.
     */
    public static final int RET_COMPRESS_FILE_FAIL= 20034;

    /**
     *  build sdk project fail.
     */
    public static final int RET_BUILD_SDK_FAIL= 20035;

    /**
     *   read file fail.
     */
    public static final int RET_READ_FILE_FAIL= 20036;

    /**
     *   write file fail.
     */
    public static final int RET_WRITE_FILE_FAIL= 20037;


    /**
     *  service info doesn't exist in yaml.
     */
    public static final int RET_CHECK_SERVICE_EXIST_IN_YAML_FAIL= 20038;

    /**
     *   restful request fail.
     */
    public static final int RET_RESTFUL_REQUEST_FAIL= 20039;

    /**
     *   copy file fail.
     */
    public static final int RET_COPY_FILE_FAIL= 20040;

    /**
     *   synchronize pkg fail.
     */
    public static final int RET_SYNCHRONIZE_APP_PKG_FAIL= 20041;

    /**
     *   publish pkg fail.
     */
    public static final int RET_PUBLISH_APP_PKG_FAIL= 20042;


}
