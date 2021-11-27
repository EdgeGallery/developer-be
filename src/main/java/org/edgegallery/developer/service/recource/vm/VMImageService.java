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

    VMImageRes getVmImages(VMImageReq vmImageReq);

    VMImage getVmImageById(Integer imageId);

    Boolean createVmImage(VMImage vmImage);

    Boolean updateVmImage(VMImage vmImage, Integer imageId);

    Boolean deleteVmImage(Integer imageId);

    Boolean publishVmImage(Integer imageId);

    Boolean resetImageStatus(Integer imageId);

    ResponseEntity uploadVmImage(HttpServletRequest request, Chunk chunk, Integer imageId);

    List<Integer> checkUploadedChunks(Integer imageId, String identifier);

    ResponseEntity cancelUploadVmImage(Integer imageId, String identifier);

    ResponseEntity mergeVmImage(String fileName, String identifier, Integer imageId);

    byte[] downloadVmImage(Integer imageId);

    OperationInfoRep imageSlim(Integer imageId);

    VMImage createVmImageAllInfo(VMImage vmImage);

}
