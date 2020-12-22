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

package org.edgegallery.developer.interfaces.plugin.facade.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.domain.model.plugin.Plugin;

@Getter
@Setter
public class PluginDto {

    /**
     * plugin id,Use database strategy uuid_generate_v4 (), automatically generated.
     */
    private String pluginId;

    private String pluginName;

    /**
     * plugin introduction,can be null or empty.
     */
    private String introduction;

    private float satisfaction;

    /**
     * only be one of{JAVA,Python,Go,.Net,PHP}.
     */
    private String codeLanguage;

    /**
     * pluginType:1 Plugin pluginType:2 SDK.
     */
    private String pluginType;

    /**
     * can be null or empty.
     */
    private String version;

    private int downloadCount;

    private String logoFile;

    private String pluginFile;

    private Date uploadTime;

    private String userId;

    private String userName;

    private long pluginSize;

    private String apiFile;

    private int scoreCount;

    /**
     *  transfer plugin to PluginDto.
     */
    public static PluginDto of(Plugin plugin) {
        PluginDto pluginDto = new PluginDto();
        pluginDto.setApiFile("");
        pluginDto.setCodeLanguage(plugin.getCodeLanguage());
        pluginDto.setDownloadCount(plugin.getDownloadCount());
        pluginDto.setIntroduction(plugin.getIntroduction());
        pluginDto.setLogoFile("");
        pluginDto.setPluginFile("");
        pluginDto.setPluginId(plugin.getPluginId());
        pluginDto.setPluginName(plugin.getPluginName());
        pluginDto.setPluginType(plugin.getPluginType());
        pluginDto.setSatisfaction(plugin.getSatisfaction());
        pluginDto.setScoreCount(plugin.getScoreCount());
        pluginDto.setUploadTime((Date) plugin.getUploadTime().clone());
        pluginDto.setUserId(plugin.getUser().getUserId());
        pluginDto.setUserName(plugin.getUser().getUserName());
        pluginDto.setVersion(plugin.getVersion());
        pluginDto.setPluginSize(Math.toIntExact(plugin.getPluginFile().getSize()));
        return pluginDto;
    }

    public Date getUploadTime() {
        return (Date)this.uploadTime.clone();
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = (Date)uploadTime.clone();
    }
}
