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

    /**
     * download uploaded file.
     *
     * @param uploadedFile uploaded file
     * @param userId userId
     * @return
     */
    byte[] getFileStream(UploadFile uploadedFile, String userId);

    /**
     * get upload file.
     *
     * @param fileId fileId
     * @return
     */
    UploadFile getFile(String fileId);

    /**
     * save file.
     *
     * @param uploadFile upload file
     * @return
     */
    int saveFile(UploadFile uploadFile);

    /**
     * upload file.
     *
     * @param userId userId
     * @param fileType file Type(icon,api,md,script)
     * @param uploadFile file
     * @return
     */
    UploadFile uploadFile(String userId, String fileType, MultipartFile uploadFile);

    /**
     * delete file.
     *
     * @param fileId fileId
     * @return
     */
    boolean deleteFile(String fileId);

    /**
     * download sample code.
     *
     * @param apiFileIds fileId list
     * @return
     */
    byte[] downloadSampleCode(List<String> apiFileIds);

    /**
     * get sample code structure.
     *
     * @param apiFileIds fileId list
     * @return
     */
    AppPkgStructure getSampleCodeStru(List<String> apiFileIds);

    /**
     * get sample code content.
     *
     * @param apiFileIds fileId list
     * @param fileName file name
     * @return
     */
    String getSampleCodeContent(List<String> apiFileIds, String fileName);

    /**
     * download sdk.
     *
     * @param fileId file id
     * @param lan language
     * @param capabilities capability list
     * @return
     */
    byte[] getSdkProject(String fileId, String lan, List<Capability> capabilities);

    /**
     * save file locally.
     *
     * @param uploadFile upload file
     * @param userId user id
     * @return
     */
    UploadFile saveFileToLocal(MultipartFile uploadFile, String userId);

}
