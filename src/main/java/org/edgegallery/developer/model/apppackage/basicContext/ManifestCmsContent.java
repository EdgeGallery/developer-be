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

package org.edgegallery.developer.model.apppackage.basicContext;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.apppackage.IToscaContentEnum;

@Getter
public enum ManifestCmsContent implements IToscaContentEnum {

    BEGIN_CMS("-----BEGIN CMS-----", true),
    CONTENT_CMS(".*", true),
    END_CMS("-----END CMS-----", true);

    private final String name;

    private final boolean isNotNull;

    private final String split = "";

    ManifestCmsContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    @Override
    public IToscaContentEnum of(String name) {
        for (ManifestCmsContent type : ManifestCmsContent.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        if (!StringUtils.isEmpty(name)) {
            return CONTENT_CMS;
        }
        return null;
    }

    /**
     * to check the value is right.
     */
    public boolean check(String value) {
        return !this.isNotNull() || !StringUtils.isEmpty(value);
    }

    /**
     * format to string this value.
     */
    @Override
    public String toString(String value) {
        switch (this) {
            case BEGIN_CMS:
                return BEGIN_CMS.getName();
            case END_CMS:
                return END_CMS.getName();
            case CONTENT_CMS:
                return value;
            default:
                return "";
        }
    }
}
