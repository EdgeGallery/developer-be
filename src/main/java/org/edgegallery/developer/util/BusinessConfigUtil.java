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

package org.edgegallery.developer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BusinessConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessConfigUtil.class);

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = BusinessConfigUtil.class.getClassLoader()
            .getResourceAsStream("business.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Failed to read resource file. {}", e.getMessage());
        }
    }

    private BusinessConfigUtil() {
    }

    public static String getPluginPath() {
        return PROPERTIES.getProperty("remote.plugin");
    }

    public static String getAppPath() {
        return PROPERTIES.getProperty("remote.app");
    }

    public static String getWorkspacePath() {
        return PROPERTIES.getProperty("remote.workspace");
    }

    public static String getUploadfilesPath() {
        return PROPERTIES.getProperty("remote.uploadfiles");
    }

}
