package org.edgegallery.developer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.ReleaseConfig;

@Mapper
public interface ReleaseConfigMapper {

    int saveConfig(ReleaseConfig config);

    ReleaseConfig getConfigByReleaseId(String releaseId);

    ReleaseConfig getConfigByProjectId(@Param("projectId") String projectId);

    int modifyReleaseConfig(ReleaseConfig config);

    int updateATPStatus(ReleaseConfig config);

}
