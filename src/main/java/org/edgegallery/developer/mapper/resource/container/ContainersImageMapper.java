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

package org.edgegallery.developer.mapper.resource.container;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.resource.container.ContainerImage;
import org.edgegallery.developer.model.resource.container.ContainerImageReq;

@Mapper
public interface ContainersImageMapper {

    int createContainerImage(ContainerImage containerImage);

    int deleteContainerImageById(@Param("imageId") String imageId);

    ContainerImage getContainerImage(@Param("imageId") String imageId);

    List<ContainerImage> getAllImage();

    List<ContainerImage> getAllImageByAdmin();

    List<ContainerImage> getAllImageByAdminAuth(ContainerImageReq containerImageReq);

    List<ContainerImage> getAllImageByOrdinaryAuth(ContainerImageReq containerImageReq);

    int updateContainerImageType(@Param("imageId") String imageId, @Param("userId") String userId,
        @Param("imageType") String imageType);

    int updateContainerImage(@Param("oldImageId") String imageId, @Param("containerImage") ContainerImage containerImage);

}
