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

package org.edgegallery.developer.service.apppackage.csar.filehandler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.apppackage.IToscaContentEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentParseHandlerImpl implements IContentParseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentParseHandlerImpl.class);

    private final Map<IToscaContentEnum, String> params = new LinkedHashMap<>();

    private final Class<?> contextEnum;

    ContentParseHandlerImpl(Class<?> contextEnum) {
        this.contextEnum = contextEnum;
    }

    @Override
    public void addOneData(String line) {
        Map.Entry<String, String> data = parseThisLine(line);
        IToscaContentEnum contentEnum = (IToscaContentEnum) contextEnum.getEnumConstants()[0];
        IToscaContentEnum type = contentEnum.of(data.getKey());
        if (type != null) {
            params.put(type, data.getValue());
        }
    }

    @Override
    public Map<IToscaContentEnum, String> getParams() {
        return params;
    }

    private Map.Entry<String, String> parseThisLine(String line) {
        int splitIndex = line.indexOf(":");
        String key;
        String value;
        if (splitIndex > 0) {
            key = line.substring(0, splitIndex).trim();
            value = line.substring(splitIndex + 1).trim();
        } else {
            key = line;
            value = line;
        }
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    @Override
    public boolean checkParams() {
        for (Object type : contextEnum.getEnumConstants()) {
            if (type instanceof IToscaContentEnum) {
                IToscaContentEnum appdContextDef = (IToscaContentEnum) type;
                if (!appdContextDef.check(params.get(appdContextDef))) {
                    LOGGER.info("not include param {} in the MF file.", appdContextDef.getName());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();
        params.forEach((key, value) -> lines.add(key.toString(value)));
        return StringUtils.join(lines, "\n");
    }

    @Override
    public Map.Entry<String, String> getFirstData() {
        IToscaContentEnum contentEnum = (IToscaContentEnum) contextEnum.getEnumConstants()[0];
        return new AbstractMap.SimpleEntry<>(contentEnum.getName(), params.get(contentEnum));
    }
}
