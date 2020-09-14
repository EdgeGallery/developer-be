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

package org.edgegallery.developer.application.plugin;

import java.io.IOException;
import java.io.InputStream;
import org.edgegallery.developer.domain.model.comment.PluginDownloadRecord;
import org.edgegallery.developer.domain.model.plugin.ApiChecker;
import org.edgegallery.developer.domain.model.plugin.Plugin;
import org.edgegallery.developer.domain.model.plugin.PluginRepository;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.service.FileService;
import org.edgegallery.developer.domain.shared.AFile;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.domain.shared.IconChecker;
import org.edgegallery.developer.domain.shared.PluginChecker;
import org.edgegallery.developer.domain.shared.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("pluginService")
public class PluginService {

    @Autowired
    private PluginRepository pluginRepository;

    @Autowired
    private FileService fileService;

    /**
     * publish plugin.
     *
     * @param newPlugin object of Plugin.
     * @param pluginFile file of plugin.
     * @param logoFile logo file of plugin.
     * @param apiFile file of api, used for describe api.
     * @return
     */
    @Transactional
    public Plugin publish(Plugin newPlugin, MultipartFile pluginFile, MultipartFile logoFile, MultipartFile apiFile)
        throws IOException {
        AFile plugin = getFile(pluginFile, new PluginChecker());
        AFile logo = getFile(logoFile, new IconChecker());
        AFile api = getFile(apiFile, new ApiChecker());
        newPlugin.updateFile(logo, plugin, api);
        pluginRepository.store(newPlugin);
        return newPlugin;
    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker) throws IOException {
        String fileAddress = fileService.saveTo(file, fileChecker);
        return new AFile(file.getOriginalFilename(), fileAddress, file.getSize());
    }

    /**
     * delete plugin by plugin id.
     *
     * @param pluginId id of plugin
     */
    @Transactional
    public void deleteByPluginId(String pluginId) {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        fileService.delete(plugin.getApiFile());
        fileService.delete(plugin.getLogoFile());
        fileService.delete(plugin.getPluginFile());
        pluginRepository.remove(plugin);
    }

    /**
     * download plugin by plugin id.
     *
     * @param pluginId id of plugin
     * @return
     */
    public InputStream download(String pluginId) throws IOException {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        return fileService.get(plugin.getPluginFile());
    }

    /**
     * download plugin logo by plugin id.
     *
     * @param pluginId id of plugin
     * @return
     */
    public InputStream downloadLogo(String pluginId) throws IOException {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        return fileService.get(plugin.getLogoFile());
    }

    /**
     * download api file by plugin id.
     *
     * @param pluginId id of plugin
     * @return
     */
    public InputStream downloadApiFile(String pluginId) throws IOException {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        return fileService.get(plugin.getApiFile());
    }

    /**
     * mark a comment record by user.
     *
     * @param pluginId id of plugin.
     * @param score score by user give.
     * @param user user who do mark.
     * @return
     */
    public Plugin mark(String pluginId, Integer score, User user) {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        plugin.mark(score);
        PluginDownloadRecord record = new PluginDownloadRecord(pluginId, user, score, 0);
        pluginRepository.saveDownloadRecord(record);
        pluginRepository.store(plugin);
        return plugin;
    }
}
