package org.edgegallery.developer.util;

import java.util.List;
import javax.annotation.PostConstruct;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImageUtils {

    @Autowired
    private ProjectImageMapper projectImageMapper;

    public static ImageUtils imageUtils;

    @PostConstruct
    public void init() {
        imageUtils = this;
        imageUtils.projectImageMapper = this.projectImageMapper;
    }

    public static List<ProjectImageConfig> getAllImage(String projectId) {
        return imageUtils.projectImageMapper.getAllImage(projectId);
    }
}
