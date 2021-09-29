package org.edgegallery.developer.mapper.image;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.vmimage.UploadFileInfo;
import org.edgegallery.developer.model.vmimage.VMImage;

public interface VMImageMapper {
    Integer getVmImagesCount(Map map);

    List<VMImage> getVmImagesByCondition(Map map);

    VMImage getVmImage(Integer imageId);

    String getVmImagesPath(Integer imageId);

    Integer getVmNameCount(@Param("name") String systemName, @Param("imageId") Integer imageId,
        @Param("userId") String userId);

    int createVmImage(VMImage vmSystem);

    int updateVmImage(VMImage vmSystem);

    int deleteVmImage(VMImage vmSystem);

    int updateVmImageStatus(@Param("imageId") Integer imageId, @Param("status") String status);

    int updateVmImageIdentifier(@Param("imageId") Integer imageId, @Param("identifier") String identifier);

    int updateVmImageErrorType(@Param("imageId") Integer imageId, @Param("errorType") String errorType);

    void updateVmImageUploadInfo(UploadFileInfo uploadFileInfo);

    void updateVmImageSlimStatus(@Param("imageId")Integer imageId, @Param("slimStatus")String slimStatus);

}
