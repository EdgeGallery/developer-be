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

package org.edgegallery.developer.service.application.common;

public class IContextParameter {

    private IContextParameter() {
        throw new IllegalStateException("IContextParameter class");
    }

    public static final String PARAM_APPLICATION_ID = "application_id";

    public static final String PARAM_VM_ID = "vm_id";

    public static final String PARAM_PACKAGE_ID = "package_id";

    public static final String PARAM_MEPM_PACKAGE_ID = "mepm_package_id";

    public static final String PARAM_APP_INSTANCE_ID = "app_instance_id";

    public static final String PARAM_VM_INSTANCE_ID = "vm_instance_id";

    public static final String PARAM_IMAGE_INSTANCE_ID = "image_instance_id";

}
