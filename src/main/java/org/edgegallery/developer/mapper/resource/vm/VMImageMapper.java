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

package org.edgegallery.developer.mapper.resource.vm;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.resource.vm.UploadFileInfo;
import org.edgegallery.developer.model.resource.vm.VMImage;

public interface VMImageMapper {
    Integer getVmImagesCount(Map map);

    List<VMImage> getVmImagesByCondition(Map map);

    VMImage getVmImage(Integer imageId);

    String getVmImagesPath(Integer imageId);

    Integer getVmNameCount(@Param("name") String name, @Param("imageId") Integer imageId,
        @Param("userId") String userId);

    int createVmImage(VMImage vmSystem);

    int updateVmImage(VMImage vmSystem);

    int deleteVmImage(VMImage vmSystem);

    int updateVmImageStatus(@Param("imageId") Integer imageId, @Param("status") String status);

    int updateVmImageIdentifier(@Param("imageId") Integer imageId, @Param("identifier") String identifier);

    int updateVmImageErrorType(@Param("imageId") Integer imageId, @Param("errorType") String errorType);

    void updateVmImageUploadInfo(UploadFileInfo uploadFileInfo);

    void updateVmImageSlimStatus(@Param("imageId")Integer imageId, @Param("imageSlimStatus")String imageSlimStatus);

    int updateVmImageInfo(@Param("imageId") Integer imageId, @Param("imageSize") Long imageSize, @Param("fileMd5") String fileMd5);

}
