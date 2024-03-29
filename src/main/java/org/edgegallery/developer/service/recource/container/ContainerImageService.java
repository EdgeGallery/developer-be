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

package org.edgegallery.developer.service.recource.container;

import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.resource.container.ContainerImage;
import org.edgegallery.developer.model.resource.container.ContainerImageReq;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface ContainerImageService {

    /**
     * upload Container Image(tar file).
     *
     * @return
     */
    ResponseEntity uploadContainerImage(HttpServletRequest request, Chunk chunk, String imageId);

    /**
     * merge Container Image(tar file).
     *
     * @return
     */
    ResponseEntity mergeContainerImage(String fileName, String guid, String imageId);

    /**
     * create container image record.
     *
     * @param containerImage containerImage
     * @return
     */
    @Transactional
    ContainerImage createContainerImage(ContainerImage containerImage);

    /**
     * update container image record.
     *
     * @param imageId imageId
     * @param containerImage containerImage
     * @return
     */
    @Transactional
    ContainerImage updateContainerImage(String imageId, ContainerImage containerImage);

    /**
     * get all container image record.
     *
     * @param containerImageReq containerImageReq
     * @return
     */
    @Transactional
    Page<ContainerImage> getAllImage(ContainerImageReq containerImageReq);

    /**
     * delete container image record and delete imgae of harbor repo.
     *
     * @param imageId imageId
     * @return
     */
    @Transactional
    Boolean deleteContainerImage(String imageId);

    /**
     * download container image from harbor repo.
     *
     * @param imageId imageId
     * @return
     */
    ResponseEntity<InputStreamResource> downloadHarborImage(String imageId);

    /**
     * cancel upload image.
     *
     * @param imageId imageId
     * @return
     */
    ResponseEntity cancelUploadHarborImage(String imageId);

    /**
     * synchronize container Image from harbor repo.
     *
     * @return
     */
    ResponseEntity synchronizeHarborImage();
}
