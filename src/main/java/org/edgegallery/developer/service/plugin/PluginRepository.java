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

package org.edgegallery.developer.service.plugin;

import java.util.Optional;
import org.edgegallery.developer.model.plugin.Plugin;
import org.edgegallery.developer.model.plugin.PluginPageCriteria;
import org.edgegallery.developer.model.plugin.comment.PluginDownloadRecord;
import org.edgegallery.developer.model.common.Page;

public interface PluginRepository {
    void store(Plugin plugin);

    Page<Plugin> findAllWithPagination(PluginPageCriteria pluginPageCriteria);

    Optional<Plugin> find(String pluginId);

    void remove(Plugin plugin);

    void saveDownloadRecord(PluginDownloadRecord record);

    long findPlugInByHashCode(String hashCode);
}
