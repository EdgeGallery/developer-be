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

package org.edgegallery.developer.service.uploadfile;

import java.util.List;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

    byte[] getFileStream(UploadFile uploadedFile, String userId);

    UploadFile getFile(String fileId);

    UploadFile uploadFile(String userId, String fileType, MultipartFile uploadFile);

    boolean deleteFile(String fileId);

    byte[] downloadSampleCode(List<String> apiFileIds);

    AppPkgStructure getSampleCodeStru(List<String> apiFileIds);

    String getSampleCodeContent(String fileName);

    byte[] getSdkProject(String fileId, String lan, List<Capability> capabilities);

    UploadFile saveFileToLocal(MultipartFile uploadFile, String userId);

    void moveFileToWorkSpaceById(String srcId, String applicationId);
}
