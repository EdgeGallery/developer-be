package org.edgegallery.developer.mapper.application.container;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;

@Mapper
public interface VisualConfigurationMapper {
    int saveYaml(HelmTemplateYamlPo helmTemplateYamlPo);

    List<HelmTemplateYamlPo> queryTemplateYamlByProjectId(String userId, String projectId);

    int deleteYamlByFileId(String fileId);

    HelmTemplateYamlPo queryTemplateYamlById(@Param("fileId") String fileId);

    HelmTemplateYamlPo queryTemplateYamlByType(@Param("fileId") String fileId,@Param("configType") String configType);

    int updateHelm(HelmTemplateYamlPo helmTemplateYamlPo);

    String queryProjectId(String fileId);
}
