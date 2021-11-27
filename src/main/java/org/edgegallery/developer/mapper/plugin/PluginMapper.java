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

package org.edgegallery.developer.mapper.plugin;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.plugin.PluginPO;
import org.edgegallery.developer.model.plugin.comment.PluginDownloadRecord;
import org.edgegallery.developer.model.plugin.PluginPageCriteria;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface PluginMapper {

    List<PluginPO> getAllPlugin(@Param("pluginType") Integer plugintype);

    void insert(PluginPO plugin);

    void delPlugin(@Param("pluginId") String pluginId);

    String getPluginPath(@Param("pluginId") String pluginId);

    String getLogoPath(@Param("pluginId") String pluginId);

    String getApiPath(@Param("pluginId") String pluginId);

    void update(PluginPO plugin);

    void updownloadCount(@Param("pluginId") String pluginId);

    void updateScore(Map<String, Object> map);

    int getScorecount(@Param("pluginId") String pluginId);

    float getSatisfaction(@Param("pluginId") String pluginId);

    Optional<PluginPO> getPluginById(@Param("pluginId") String pluginId);

    void saveDownloadRecord(PluginDownloadRecord record);

    Optional<PluginPO> getPluginByUploadTime(Date uploadTime);

    List<PluginPO> findAllWithPagination(@Param("criteria") PluginPageCriteria pluginPageCriteria);

    long count(@Param("criteria") PluginPageCriteria pluginPageCriteria);

    long findPlugInByHashCode(@Param("hashcode") String hashCode);
}
