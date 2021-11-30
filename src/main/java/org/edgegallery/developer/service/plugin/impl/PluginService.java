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

package org.edgegallery.developer.service.plugin.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.model.plugin.comment.PluginDownloadRecord;
import org.edgegallery.developer.util.filechecker.ApiChecker;
import org.edgegallery.developer.model.plugin.Plugin;
import org.edgegallery.developer.service.plugin.PluginRepository;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.service.plugin.PluginFileService;
import org.edgegallery.developer.model.plugin.AFile;
import org.edgegallery.developer.util.filechecker.FileChecker;
import org.edgegallery.developer.util.filechecker.IconChecker;
import org.edgegallery.developer.util.filechecker.PluginChecker;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.model.restful.FormatRespDto;
import org.edgegallery.developer.util.FileHashCode;
import org.edgegallery.developer.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.spencerwi.either.Either;

@Service("pluginService")
public class PluginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginService.class);

    @Autowired
    private PluginRepository pluginRepository;

    @Autowired
    private PluginFileService pluginFileService;

    /**
     * publish plugin over.
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
        String hashCode = FileHashCode.generateHashCode(plugin.getStorageAddress());
        if (pluginRepository.findPlugInByHashCode(hashCode) > 0) {
            LOGGER.error("this plugin file has been uploaded, hash code {}", hashCode);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "this plugin file has been uploaded.");
            throw new InvocationException(error.getEnumStatus().getStatusCode(),
                error.getEnumStatus().getReasonPhrase(), error.getErrorRespDto().getDetail());
        }
        plugin.setHashCode(hashCode);
        AFile logo = getFile(logoFile, new IconChecker());
        AFile api = getFile(apiFile, new ApiChecker());
        newPlugin.updateFile(logo, plugin, api);
        pluginRepository.store(newPlugin);
        return newPlugin;

    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker) throws IOException {
        String fileAddress = pluginFileService.saveTo(file, fileChecker);
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
        pluginFileService.delete(plugin.getApiFile());
        pluginFileService.delete(plugin.getLogoFile());
        pluginFileService.delete(plugin.getPluginFile());
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
        int downloadCount = plugin.getDownloadCount() + 1;
        plugin.setDownloadCount(downloadCount);
        pluginRepository.store(plugin);
        return pluginFileService.get(plugin.getPluginFile());
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
        return pluginFileService.get(plugin.getLogoFile());
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
        return pluginFileService.get(plugin.getApiFile());
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

    /**
     * get plugin content.
     *
     * @param pluginId id of plugin
     * @return
     */
    public Either<FormatRespDto, String> getApiContent(String pluginId) {
        Plugin plugin = pluginRepository.find(pluginId)
            .orElseThrow(() -> new EntityNotFoundException(Plugin.class, pluginId));
        AFile apiFile = plugin.getApiFile();
        if (apiFile != null) {
            String path = apiFile.getStorageAddress();
            if (StringUtils.isNotEmpty(path)) {
                String content = FileUtil.readFileContent(path);
                if (StringUtils.isNotEmpty(content)) {
                    return Either.right(content);
                }
            }
        }
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file is empty!");
        return Either.left(error);
    }
}
