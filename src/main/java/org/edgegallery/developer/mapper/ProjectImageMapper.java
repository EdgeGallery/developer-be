package org.edgegallery.developer.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;

@Mapper
public interface ProjectImageMapper {
    int saveImage(ProjectImageConfig imageConfig);

    int deleteImage(String projectId);

    List<ProjectImageConfig> getAllImage(String projectId);
}
