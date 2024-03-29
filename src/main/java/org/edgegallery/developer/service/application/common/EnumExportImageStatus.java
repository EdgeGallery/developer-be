/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.application.common;

public enum EnumExportImageStatus {

    EXPORT_IMAGE_STATUS_TIMEOUT("timeout"),

    EXPORT_IMAGE_STATUS_ERROR("error"),

    EXPORT_IMAGE_STATUS_FAILED("killed"),

    EXPORT_IMAGE_STATUS_SUCCESS("active"),

    COMPRESS_IMAGE_STATUS_SUCCESS("Success"),

    COMPRESS_IMAGE_STATUS_FAILURE("Failure");

    private String name;

    EnumExportImageStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
