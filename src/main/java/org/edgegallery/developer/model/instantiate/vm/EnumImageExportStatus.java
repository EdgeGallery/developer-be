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
package org.edgegallery.developer.model.instantiate.vm;

public enum EnumImageExportStatus {
    IMAGE_CREATING("Creating Image"),
    IMAGE_QUERYING("Querying Image Status"),
    IMAGE_DOWNLOADING("Downloading Image"),
    SUCCESS("Success"),
    FAILED("Failed");

    private String name;

    EnumImageExportStatus(String name) {
        this.name = name;
    }
}
