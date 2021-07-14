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

package org.edgegallery.developer.domain.model.plugin;

import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.edgegallery.developer.common.enums.EnumCodeLanguage;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.shared.AFile;
import org.edgegallery.developer.domain.shared.Entity;

@Setter
@Getter
public class Plugin implements Entity {

    private String pluginId;

    private String pluginName;

    private String introduction;

    private float satisfaction;

    private String codeLanguage;

    private String pluginType;

    private String version;

    private int scoreCount;

    private int downloadCount;

    private AFile logoFile;

    private AFile pluginFile;

    private Date uploadTime;

    private User user;

    private AFile apiFile;

    /**
     * Constructor of Plugin.
     */
    public Plugin(String pluginId, String pluginName, String introduction, String codeLanguage, String pluginType,
        String version) {
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.introduction = introduction;
        this.satisfaction = 3.0f;
        this.codeLanguage = codeLanguage;
        this.pluginType = pluginType;
        this.version = version;
        this.uploadTime = new Date();
    }

    /**
     * Constructor of Plugin.
     */
    public Plugin(String pluginName, String introduction, String codeLanguage, String pluginType, String version,
        User user) {

        Validate.notNull(pluginName, "PluginName is required");
        Validate.notNull(codeLanguage, "CodeLanguage is required");
        Validate.notNull(user.getUserName(), "UserName is required");
        Validate.notNull(version, "plugin version is required");
        if (Integer.parseInt(pluginType) != 1 && Integer.parseInt(pluginType) != 2) {
            throw new IllegalArgumentException("pluginType is illegal");
        }

        if (!isInclude(codeLanguage)) {
            throw new IllegalArgumentException("parameter codeLanguage can only be one of {JAVA,Python,Go,.Net,PHP}");
        }

        this.pluginId = generatePluginId();
        this.pluginName = pluginName;
        this.introduction = introduction;
        this.satisfaction = 3.0f;
        this.codeLanguage = codeLanguage;
        this.pluginType = pluginType;
        this.version = version;
        this.uploadTime = new Date();
        this.user = user;
    }

    public Plugin() {

    }

    private static boolean isInclude(String key) {
        boolean include = false;
        for (EnumCodeLanguage e : EnumCodeLanguage.values()) {
            if (key.equalsIgnoreCase(e.getValue())) {
                include = true;
                break;
            }
        }
        return include;
    }

    /**
     * get update time.
     *
     * @return update time
     */
    public Date getUploadTime() {
        if (this.uploadTime != null) {
            return new Date(this.uploadTime.getTime());
        } else {
            return null;
        }
    }

    /**
     * set update time.
     */
    public void setUploadTime(Date uploadTime) {
        if (uploadTime != null) {
            this.uploadTime = (Date) uploadTime.clone();
        } else {
            this.uploadTime = null;
        }
    }

    public String generatePluginId() {
        String random = UUID.randomUUID().toString();
        return random.replace("-", "");
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public float getSatisfaction() {
        return satisfaction;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public String getPluginType() {
        return pluginType;
    }

    public String getVersion() {
        return version;
    }

    public int getScoreCount() {
        return scoreCount;
    }

    public AFile getLogoFile() {
        return logoFile;
    }

    public AFile getPluginFile() {
        return pluginFile;
    }

    public User getUser() {
        return user;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public AFile getApiFile() {
        return apiFile;
    }

    /**
     * update file by new logo, plugin and api file.
     */
    public void updateFile(AFile logo, AFile plugin, AFile api) {
        this.apiFile = api;
        this.pluginFile = plugin;
        this.logoFile = logo;
    }

    /**
     * update obj by new plugin.
     */
    public void update(Plugin newPlugin) {
        this.pluginName = newPlugin.pluginName;
        this.introduction = newPlugin.introduction;
        this.satisfaction = 3.0f;
        this.codeLanguage = newPlugin.codeLanguage;
        this.pluginType = newPlugin.pluginType;
        this.version = newPlugin.version;
        this.logoFile = newPlugin.logoFile;
        this.pluginFile = newPlugin.pluginFile;
        this.uploadTime = new Date();
    }

    public void mark(int score) {
        this.satisfaction = (this.scoreCount * this.satisfaction + score) / (1 + this.scoreCount++);
    }

}
