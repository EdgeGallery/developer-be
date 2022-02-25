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

package org.edgegallery.developer.service.recource.vm;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.restful.VMImageReq;
import org.edgegallery.developer.model.restful.VMImageRes;
import org.springframework.http.ResponseEntity;

public interface VMImageService {

    /**
     * get vm images by query conditions.
     *
     * @param vmImageReq query conditions
     * @return
     */
    VMImageRes getVmImages(VMImageReq vmImageReq);

    /**
     * get vm image by imageId.
     *
     * @param imageId image id
     * @return
     */
    VMImage getVmImageById(Integer imageId);

    /**
     * create vm image.
     *
     * @param vmImage vmImage
     * @return
     */
    Boolean createVmImage(VMImage vmImage);

    /**
     * update vm image.
     *
     * @param vmImage vm image
     * @param imageId image id
     * @return
     */
    Boolean updateVmImage(VMImage vmImage, Integer imageId);

    /**
     * delete vm image.
     *
     * @param imageId image
     * @return
     */
    Boolean deleteVmImage(Integer imageId);

    /**
     * publish vm image.
     *
     * @param imageId image id
     * @return
     */
    Boolean publishVmImage(Integer imageId);

    /**
     * reset image status.
     *
     * @param imageId image id
     * @return
     */
    Boolean resetImageStatus(Integer imageId);

    /**
     * upload vm image.
     *
     * @param request rest request
     * @param chunk file chunk
     * @param imageId image id
     * @return
     */
    ResponseEntity uploadVmImage(HttpServletRequest request, Chunk chunk, Integer imageId);

    /**
     * check upload file chunk.
     *
     * @param imageId imageId
     * @param identifier file identifier
     * @return
     */
    List<Integer> checkUploadedChunks(Integer imageId, String identifier);

    /**
     * cancel upload image.
     *
     * @param imageId image id
     * @param identifier file identifier
     * @return
     */
    ResponseEntity cancelUploadVmImage(Integer imageId, String identifier);

    /**
     * merge vm image.
     *
     * @param fileName fileName
     * @param identifier file identifier
     * @param imageId image id
     * @return
     */
    ResponseEntity mergeVmImage(String fileName, String identifier, Integer imageId);

    /**
     * download vm image.
     *
     * @param imageId imageId
     * @return
     */
    ResponseEntity<byte[]> downloadVmImage(Integer imageId);

    /**
     * slim vm image.
     *
     * @param imageId image id
     * @return
     */
    OperationInfoRep imageSlim(Integer imageId);

    /**
     * create vm image by all info.
     *
     * @param vmImage vm image
     * @return
     */
    VMImage createVmImageAllInfo(VMImage vmImage);

}
