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

package org.edgegallery.developer.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.ContainerImageReq;

public interface ContainerImageMapper {

    int createContainerImage(ContainerImage containerImage);

    int updateContainerImageByAdmin(ContainerImage containerImage);

    int updateContainerImageByOrdinary(ContainerImage containerImage);

    int deleteContainerImageByAdmin(@Param("imageId") String imageId);

    int deleteContainerImageByOrdinary(@Param("imageId") String imageId, @Param("userId") String userId,
        @Param("userName") String userName);

    int updateContainerImageStatus(@Param("imageId") String imageId, @Param("imageStatus") String imageStatus);

    int updateContainerImagePath(@Param("imageId") String imageId, @Param("imagePath") String imagePath);

    ContainerImage getContainerImage(@Param("imageId") String imageId);

    List<ContainerImage> getAllImage();

    List<ContainerImage> getAllImageByAdminAuth(Map map);

    List<ContainerImage> getAllImageByOrdinaryAuth(Map map);
}
