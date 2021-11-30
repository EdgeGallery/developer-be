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

package org.edgegallery.developer.model.resource.vm;

import lombok.Getter;

@Getter
/**
 *  task status
 */
public enum EnumProcessErrorType {
    /**
     * upload to file system failed.
     */
    FILESYSTEM_UPLOAD_FAILED("failedOnUploadToFS"),
    /**
     * format mistake.
     */
    FORMAT_MISTAKE("formatMistake"),
    /**
     * zip open failed.
     */
    OPEN_FAILED("zipFileOpenFailed"),
    /**
     * file system merge failed.
     */
    FILESYSTEM_MERGE_FAILED("fileSystemMergeFailed"),

    /**
     * file system check failed.
     */
    FILESYSTEM_CHECK_FAILED("filesystemCheckFailed");

    private String errorType;

    EnumProcessErrorType(String errorType) {
        this.errorType = errorType;
    }
}
