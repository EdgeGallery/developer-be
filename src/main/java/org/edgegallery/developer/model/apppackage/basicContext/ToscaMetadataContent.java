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

/**
 * TOSCA-Metadata: metadata.
 */
@Getter
public enum ToscaMetadataContent implements IToscaContentEnum {
    TOSCA_META_FILE_VERSION("TOSCA-Meta-File-Version", true),
    CSAR_VERSION("CSAR-Version", true),
    CREATED_BY("Created-by", true),
    ENTRY_DEFINITIONS("Entry-Definitions", true);

    private final String name;

    private final boolean isNotNull;

    private final String split = ": ";

    ToscaMetadataContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    /**
     * create enum from name.
     */
    public IToscaContentEnum of(String name) {
        for (ToscaMetadataContent type : ToscaMetadataContent.values()) {
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
