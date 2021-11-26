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

package org.edgegallery.developer.model.instantiate;

public enum EnumAppInstantiateStatus {
    PACKAGE_GENERATING("Generating Package"),
    PACKAGE_GENERATE_FAILED("Generate Package Failed"),
    PACKAGE_GENERATE_SUCCESS("Generate Package Success"),
    PACKAGE_DISTRIBUTING("Distributing Package"),
    PACKAGE_DISTRIBUTE_FAILED("Distribute Package Failed"),
    PACKAGE_DISTRIBUTE_SUCCESS("Distribute Package Success"),
    INSTANTIATING("Instantiating"),
    INSTANTIATE_FAILED("Instantiate Failed"),
    INSTANTIATE_SUCCESS("Instantiate Success"),
    SUCCESS("Success");
    private String name;

    EnumAppInstantiateStatus(String name) {
        this.name = name;
    }
}
