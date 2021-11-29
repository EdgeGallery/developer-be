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

package org.edgegallery.developer.model.plugin;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.common.User;

@Getter
@Setter
@ToString
public class PluginPO {

    public static final int TYPE_PLUGIN = 1;

    public static final int TYPE_SDK = 2;

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
    private Integer pluginType;

    /**
     * can be null or empty.
     */
    private String version;

    private int downloadCount;

    private String logoFile;

    private String pluginFile;

    private String pluginFileHashCode;

    private Date uploadTime;

    private String userId;

    private String userName;

    private int pluginSize;

    private String apiFile;

    private int scoreCount;

    /**
     * transfer plugin to PluginDto.
     *
     * @return
     */
    public static PluginPO of(Plugin plugin) {
        PluginPO pluginPO = new PluginPO();
        pluginPO.setApiFile(plugin.getApiFile().getStorageAddress());
        pluginPO.setCodeLanguage(plugin.getCodeLanguage());
        pluginPO.setDownloadCount(plugin.getDownloadCount());
        pluginPO.setIntroduction(plugin.getIntroduction());
        pluginPO.setLogoFile(plugin.getLogoFile().getStorageAddress());
        pluginPO.setPluginFile(plugin.getPluginFile().getStorageAddress());
        pluginPO.setPluginId(plugin.getPluginId());
        pluginPO.setPluginName(plugin.getPluginName());
        pluginPO.setPluginType(Integer.valueOf(plugin.getPluginType()));
        pluginPO.setSatisfaction(plugin.getSatisfaction());
        pluginPO.setScoreCount(plugin.getScoreCount());
        pluginPO.setUploadTime((plugin.getUploadTime() == null) ? null : (Date) plugin.getUploadTime().clone());
        pluginPO.setUserId(plugin.getUser().getUserId());
        pluginPO.setUserName(plugin.getUser().getUserName());
        pluginPO.setVersion(plugin.getVersion());
        pluginPO.setPluginSize(Math.toIntExact(plugin.getPluginFile().getSize()));
        pluginPO.setPluginFileHashCode(plugin.getPluginFile().getHashCode());
        return pluginPO;
    }

    /**
     * toDomainModel transfer to Plugin object.
     *
     * @return
     */
    public Plugin toDomainModel() {
        Plugin plugin = new Plugin();
        plugin.setApiFile(new AFile("apiFileName", apiFile, 1));
        plugin.setCodeLanguage(codeLanguage);
        plugin.setDownloadCount(downloadCount);
        plugin.setIntroduction(introduction);
        plugin.setLogoFile(new AFile("logoFileName", logoFile, 11));
        plugin.setPluginFile(new AFile("pluginFileName", pluginFile, 1));
        plugin.setPluginId(pluginId);
        plugin.setPluginName(pluginName);
        plugin.setPluginType(pluginType.toString());
        plugin.setSatisfaction(satisfaction);
        plugin.setScoreCount(scoreCount);
        plugin.setUploadTime(uploadTime);
        plugin.setUser(new User(userId, userName));
        plugin.setVersion(version);
        return plugin;
    }

    /**
     * getUploadTime.
     */
    public Date getUploadTime() {
        if (this.uploadTime != null) {
            return new Date(this.uploadTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * setUploadTime.
     */
    public void setUploadTime(Date uploadTime) {
        if (uploadTime != null) {
            this.uploadTime = (Date) uploadTime.clone();
        } else {
            this.uploadTime = null;
        }
    }
}
