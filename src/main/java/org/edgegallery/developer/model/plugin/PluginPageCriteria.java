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

import org.edgegallery.developer.service.plugin.impl.shared.PageCriteria;

public class PluginPageCriteria extends PageCriteria {

    private Integer type;

    private String pluginName;

    private String codeLanguage;

    /**
     * create a page object.
     *
     * @param limit max count one page
     * @param offset start with
     * @param type plugin type
     */
    public PluginPageCriteria(int limit, String codeLanguage, String pluginName, long offset, String type) {
        super(limit, offset);

        this.codeLanguage = codeLanguage;
        this.pluginName = pluginName;
        try {
            this.type = Integer.valueOf(type);
        } catch (NumberFormatException e) {
            this.type = PluginPO.TYPE_PLUGIN;
        }
    }

    public Integer getType() {
        return type;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public String getPluginName() {
        return pluginName;
    }

}
