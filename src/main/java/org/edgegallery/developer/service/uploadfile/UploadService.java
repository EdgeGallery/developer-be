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
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    ResponseEntity<byte[]> getFile(String fileId, String userId, String type);

    UploadedFile getApiFile(String fileId, String userId);

    UploadedFile uploadMdFile(String userId, MultipartFile uploadFile);

    UploadedFile uploadPicFile(String userId, MultipartFile uploadFile);

    UploadedFile uploadApiFile(String userId, MultipartFile uploadFile);

    ResponseEntity<byte[]> downloadSampleCode(List<String> apiFileIds);

    AppPkgStructure getSampleCodeStru(List<String> apiFileIds);

    String getSampleCodeContent(String fileName);

    ResponseEntity<byte[]> getSdkProject(String fileId, String lan);

    UploadedFile saveFileToLocal(MultipartFile uploadFile, String userId);

    void moveFileToWorkSpaceById(String srcId, String applicationId);
}
