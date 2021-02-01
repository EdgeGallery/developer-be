package org.edgegallery.developer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;

@Mapper
public interface ProjectImageMapper {
    int saveImage(ProjectImageConfig imageConfig);
}
