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

package org.edgegallery.developer.interfaces.plugin.facade;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.application.plugin.PluginService;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.model.plugin.Plugin;
import org.edgegallery.developer.domain.model.plugin.PluginPageCriteria;
import org.edgegallery.developer.domain.model.plugin.PluginRepository;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.interfaces.plugin.facade.dto.PluginDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PluginServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginServiceFacade.class);

    @Autowired
    private PluginService pluginService;

    @Autowired
    private PluginRepository pluginRepository;

    /**
     * upload plugin.
     */
    public PluginDto publish(Plugin newPlugin, MultipartFile pluginFile, MultipartFile logoFile, MultipartFile apiFile)
        throws IOException {
        return PluginDto.of(pluginService.publish(newPlugin, pluginFile, logoFile, apiFile));
    }

    /**
     * query plugin.
     */
    public Page<PluginDto> query(String pluginType, String codeLanguage, String pluginName, int limit, int offset) {
        return pluginRepository
            .findAllWithPagination(new PluginPageCriteria(limit, codeLanguage, pluginName, offset, pluginType))
            .map(PluginDto::of);
    }

    /**
     * Delete plugin by plugin id and user id.
     */
    public void deleteByPluginId(String pluginId, String userId) {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        if (!plugin.getUser().getUserId().equals(userId)) {
            LOGGER.warn("The user is not the owner of the plugin.");
            return;
        }
        pluginService.deleteByPluginId(pluginId);
    }

    /**
     * Get plugin name by pluginId.
     */
    public String getPluginName(String pluginId) {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        return plugin.getPluginFile().getName();
    }

    /**
     * get plugin.
     */
    public InputStream downloadFile(String pluginId) throws IOException {
        return pluginService.download(pluginId);
    }

    /**
     * get plugin logo.
     */
    public InputStream downloadLogo(String pluginId) throws IOException {
        return pluginService.downloadLogo(pluginId);
    }

    /**
     * get plugin api.
     */
    public InputStream downloadApiFile(String pluginId) throws IOException {
        return pluginService.downloadApiFile(pluginId);
    }

    /**
     * Update Plugin by inputs.
     */
    public PluginDto updatePlugin(Plugin newPlugin, MultipartFile pluginFile, MultipartFile logoFile,
        MultipartFile apiFile) throws IOException {
        Plugin plugin = pluginRepository.find(newPlugin.getPluginId())
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, newPlugin.getPluginId()));
        if (!plugin.getUser().getUserId().equals(AccessUserUtil.getUser().getUserId())) {
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                "The user is not the owner of this plugin.");
            throw new InvocationException(error.getEnumStatus().getStatusCode(),
                error.getEnumStatus().getReasonPhrase(), error.getErrorRespDto());
        }
        plugin.update(newPlugin);
        return PluginDto.of(pluginService.publish(plugin, pluginFile, logoFile, apiFile));
    }

    /**
     * mark plugin.
     */
    public Plugin mark(String pluginId, Integer score, User user) {
        return pluginService.mark(pluginId, score, user);
    }
}
