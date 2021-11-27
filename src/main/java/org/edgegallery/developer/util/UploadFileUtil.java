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

package org.edgegallery.developer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public final class UploadFileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String HARBOR_PROTOCOL = "https";

    private UploadFileUtil() {
    }

    /**
     * read file by line.
     */
    public static List<String> readFileByLine(File fin) {
        String line;
        List<String> sb = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(fin);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                sb.add(line + "\r\n");
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return sb;
    }

    public static String readFile(File fin) {
        String line;
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(fin);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\r\n");
            }
        } catch (IOException e) {
            LOGGER.error("read file by line occur {}", e.getMessage());
            return null;
        }
        return sb.toString();
    }

    public static String addNameSpace(String content) {
        String[] multiContent = content.split("\r\n");
        List<String> list = new ArrayList<>();
        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < multiContent.length; i++) {
            list.add(multiContent[i]);
        }
        String in = getIndexOfSameObject(list);
        String[] indexes = in.split(",");
        for (String index : indexes) {
            nums.add(Integer.parseInt(index));
        }
        for (int i = 0; i < nums.size(); i++) {
            list.add(nums.get(i) + i, "namespace: '{{ .Values.appconfig.appnamespace }}'");
        }
        StringBuilder sb = new StringBuilder();
        for (String newStr : list) {
            if (newStr.contains("namespace")) {
                sb.append("  " + newStr + "\r\n");
            } else {
                sb.append(newStr + "\r\n");
            }
        }
        return sb.toString();
    }

    private static String getIndexOfSameObject(List<String> list) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);
            String old = map.get(key);
            if (old != null) {
                map.put(key, old + "," + (i + 1));
            } else {
                map.put(key, "" + (i + 1));
            }
        }
        Set<Map.Entry<String, String>> sets = map.entrySet();
        String index = "";
        for (Map.Entry<String, String> entry : sets) {
            if (entry.getKey().startsWith("metadata")) {
                index = entry.getValue();
            }
        }
        return index;
    }

    public static String replaceContent(String content) {
        String[] multiContent = content.split("\r\n");
        for (int i = 0; i < multiContent.length; i++) {
            if (multiContent[i].contains("namespace")) {
                multiContent[i] = "namespace: '{{ .Values.appconfig.appnamespace }}'";
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String newStr : multiContent) {
            if (newStr.contains("namespace")) {
                sb.append("  " + newStr + "\r\n");
            } else if (newStr.contains("{{- if .Values.global.mepagent.enabled }}") || newStr.contains("{{- end }}")) {
                sb.append(newStr.replace(newStr, ""));
            } else {
                sb.append(newStr + "\r\n");
            }
        }
        return sb.toString();
    }
}

