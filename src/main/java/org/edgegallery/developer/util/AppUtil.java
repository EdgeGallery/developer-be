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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.edgegallery.developer.model.AppPackageBasicInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppUtil {

    private static final String CSAR_EXTENSION = ".csar";

    private static final String PACKAGE_XML_FORMAT = "xml";

    private static final String PACKAGE_YAML_FORMAT = "yaml";

    private static final String MANIFEST = ".mf";

    private static final String MARKDOWN = ".md";

    private static final String MF_VERSION_META = "app_archive_version";

    private static final String MF_PRODUCT_NAME = "app_name";

    private static final String MF_PROVIDER_META = "app_provider";

    private static final Logger log = LoggerFactory.getLogger(AppUtil.class);

    private AppUtil() {
    }

    /**
     * getPackageBasicInfo.
     *
     * @return
     */
    public static AppPackageBasicInfo getPackageBasicInfo(String fileLocation) {
        AppPackageBasicInfo basicInfo = new AppPackageBasicInfo();
        String unzipDir = getUnzipDir(fileLocation);
        boolean isXmlCsar = false;
        try {
            String tempfolder = unzipDir;
            List<String> unzipFiles = CsarFileUtil.unzip(fileLocation, tempfolder);
            if (unzipFiles.isEmpty()) {
                isXmlCsar = true;
            }
            for (String unzipFile : unzipFiles) {
                if (unzipFile.toLowerCase().endsWith(MANIFEST)) {
                    readManifest(basicInfo, unzipFile);
                }
                if (unzipFile.toLowerCase().endsWith(MARKDOWN)) {
                    readMarkDown(basicInfo, unzipFile);
                }

                if (isYamlFile(new File(unzipFile))) {
                    isXmlCsar = false;
                }
            }

        } catch (IOException e) {
            log.error("judge package type error ! {}", e.getMessage());
        }
        if (isXmlCsar) {
            basicInfo.setFormat(PACKAGE_XML_FORMAT);
        } else {
            basicInfo.setFormat(PACKAGE_YAML_FORMAT);
        }
        return basicInfo;
    }

    public static String getUnzipDir(String dirName) {
        File tmpDir = new File(File.separator + dirName);
        return tmpDir.getAbsolutePath().replace(CSAR_EXTENSION, "");
    }

    private static void readManifest(AppPackageBasicInfo basicInfo, String unzipFile) throws IOException {
        // Fix the package type to CSAR, temporary
        File file = new File(unzipFile);
        InputStreamReader fileReader = new InputStreamReader(FileUtils.openInputStream(file), StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(fileReader)) {
            LineIterator lineIterator = new LineIterator(reader);
            while (lineIterator.hasNext()) {
                String tempString = lineIterator.next();
                // If line is empty, ignore
                if ("".equals(tempString)) {
                    continue;
                }
                int count1 = tempString.indexOf(":");
                String meta = tempString.substring(0, count1).trim();
                // Check for the package provider name
                if (meta.equalsIgnoreCase(MF_PRODUCT_NAME)) {
                    int count = tempString.indexOf(":") + 1;
                    basicInfo.setAppname(tempString.substring(count).trim());
                }
                // Check for the package provider name
                if (meta.equalsIgnoreCase(MF_PROVIDER_META)) {
                    int count = tempString.indexOf(":") + 1;
                    basicInfo.setProvider(tempString.substring(count).trim());
                }
                // Check for package version
                if (meta.equalsIgnoreCase(MF_VERSION_META)) {
                    int count = tempString.indexOf(":") + 1;
                    basicInfo.setVersion(tempString.substring(count).trim());
                }
            }
        } catch (IOException e) {
            log.error("Exception while parsing manifest file" + e, e);
        }
    }

    private static void readMarkDown(AppPackageBasicInfo basicInfo, String unzipFile) {
        File file = new File(unzipFile);
        byte[] buffer = new byte[1000];
        StringBuilder detailContent = new StringBuilder();
        try (FileInputStream in = FileUtils.openInputStream(file)) {
            while (in.read(buffer) > 0) {
                detailContent.append(new String(buffer, StandardCharsets.UTF_8));
            }
            basicInfo.setDetailContent(detailContent.toString());
        } catch (IOException e) {
            log.error("md file read error. {}", e.getMessage());
        }
    }

    private static boolean isYamlFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(".yaml");
    }

}
