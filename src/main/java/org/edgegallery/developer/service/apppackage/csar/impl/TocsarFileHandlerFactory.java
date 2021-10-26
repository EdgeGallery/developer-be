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

package org.edgegallery.developer.service.apppackage.csar.impl;

import org.edgegallery.developer.model.apppackage.basicContext.ManifestCmsContent;
import org.edgegallery.developer.model.apppackage.basicContext.ManifestFiledataContent;
import org.edgegallery.developer.model.apppackage.basicContext.ManifestMetadataContent;
import org.edgegallery.developer.model.apppackage.basicContext.ToscaMetadataContent;
import org.edgegallery.developer.model.apppackage.basicContext.ToscaSourceContent;
import org.edgegallery.developer.model.apppackage.basicContext.VnfdToscaMetaContent;
import org.edgegallery.developer.service.apppackage.csar.IACsarFile;

public final class TocsarFileHandlerFactory {

    public static final int TOSCA_META_FILE = 1;

    public static final int MF_FILE = 2;

    public static final int VNFD_META_FILE = 3;

    private TocsarFileHandlerFactory() {
    }

    /**
     * create handler by file type.
     *
     * @param fileType TOSCA_META_FILE or MF_FILE or VNFD_META_FILE
     * @return ToscaFileHandler
     */
    public static IACsarFile createFileHandler(int fileType) {
        switch (fileType) {
            case VNFD_META_FILE:
                return new TocsaFileHandler(VnfdToscaMetaContent.class, ToscaSourceContent.class);
            case TOSCA_META_FILE:
                return new TocsaFileHandler(ToscaMetadataContent.class, ToscaSourceContent.class);
            case MF_FILE:
                return new TocsaFileHandler(ManifestMetadataContent.class, ManifestFiledataContent.class,
                    ManifestCmsContent.class);
            default:
                return null;
        }
    }
}
