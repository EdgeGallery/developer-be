/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;

@Mapper
public interface HelmTemplateYamlMapper {
    int saveYaml(HelmTemplateYamlPo helmTemplateYamlPo);

    List<HelmTemplateYamlPo> queryTemplateYamlByProjectId(String userId, String projectId);

    int deleteYamlByFileId(String fileId);

    HelmTemplateYamlPo queryTemplateYamlById(@Param("fileId") String fileId);

    HelmTemplateYamlPo queryTemplateYamlByType(@Param("fileId") String fileId,@Param("configType") String configType);

    int updateHelm(HelmTemplateYamlPo helmTemplateYamlPo);
}
