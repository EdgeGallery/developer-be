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

package org.edgegallery.developer.model.apppackage.basiccontext;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.apppackage.IToscaContentEnum;

/**
 * manifest-file: metadata info.
 */
@Getter
public enum ManifestMetadataContent implements IToscaContentEnum {
    METADATA("metadata", true),
    APP_PRODUCT_NAME("app_product_name", true),
    APP_PROVIDER_ID("app_provider_id", true),
    APP_PACKAGE_VERSION("app_package_version", true),
    APP_RELEASE_DATA_TIME("app_release_data_time", true),
    APP_TYPE("app_type", false),
    APP_CLASS("app_class", true),
    APP_PACKAGE_DESCRIPTION("app_package_description", false);

    private final String name;

    private final boolean isNotNull;

    private final String split = ": ";

    ManifestMetadataContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    /**
     * create enum from name.
     */
    public IToscaContentEnum of(String name) {
        for (ManifestMetadataContent type : ManifestMetadataContent.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public boolean check(String value) {
        return !this.isNotNull() || !StringUtils.isEmpty(value);
    }

    @Override
    public String toString(String value) {
        return ToscaFileUtil.toStringBy(this, value);
    }
}
