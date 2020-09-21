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

package org.edgegallery.developer.infrastructure.persistence.plugin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.edgegallery.developer.domain.model.comment.PluginDownloadRecord;
import org.edgegallery.developer.domain.model.plugin.Plugin;
import org.edgegallery.developer.domain.model.plugin.PluginPageCriteria;
import org.edgegallery.developer.domain.model.plugin.PluginRepository;
import org.edgegallery.developer.domain.shared.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PluginRepositoryImpl implements PluginRepository {

    @Autowired
    private PluginMapper pluginMapper;

    @Override
    public void store(Plugin plugin) {
        PluginPO pluginPO = PluginPO.of(plugin);
        Optional<PluginPO> existed = pluginMapper.getPluginById(plugin.getPluginId());
        if (existed.isPresent()) {
            pluginMapper.update(pluginPO);
        } else {
            pluginMapper.insert(pluginPO);
        }
    }

    @Override
    public Page<Plugin> findAllWithPagination(PluginPageCriteria pluginPageCriteria) {
        long total = pluginMapper.count(pluginPageCriteria);
        List<Plugin> pluginList = pluginMapper.findAllWithPagination(pluginPageCriteria)
            .stream().map(PluginPO::toDomainModel).collect(Collectors.toList());
        return new Page<>(pluginList, pluginPageCriteria.getLimit(), pluginPageCriteria.getOffset(), total);
    }

    @Override
    public Optional<Plugin> find(String pluginId) {
        return pluginMapper.getPluginById(pluginId).map(PluginPO::toDomainModel);
    }

    @Override
    public void remove(Plugin plugin) {
        pluginMapper.delPlugin(plugin.getPluginId());
    }

    @Override
    public void saveDownloadRecord(PluginDownloadRecord record) {
        pluginMapper.saveDownloadRecord(record);
    }
}
