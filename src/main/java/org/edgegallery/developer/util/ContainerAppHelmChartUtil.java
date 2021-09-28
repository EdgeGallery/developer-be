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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class ContainerAppHelmChartUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppHelmChartUtil.class);

    private ContainerAppHelmChartUtil() {

    }

    /**
     * replace namespace.
     *
     * @param multipartFile uploaded file
     * @return
     */
    public static String replaceNamesapce(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String[] nameSuffixes = {"yaml", "yml", "YAML", "YML"};
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!Arrays.asList(nameSuffixes).contains(suffix)) {
            String errMsg = "upload file is not in yaml format";
            LOGGER.error(errMsg);
            throw new IllegalRequestException(errMsg, ResponseConsts.RET_FILE_FORMAT_ERROR);
        }
        String content = "";
        File tempFile;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), null);
            multipartFile.transferTo(tempFile);
            content = UploadFileUtil.readFile(tempFile);
        } catch (IOException e) {
            String errorMsg = "Failed to read content of helm template yaml";
            LOGGER.error("Failed to read content of helm template yaml {}", e.getMessage());
            throw new FileOperateException(errorMsg, ResponseConsts.RET_READ_FILE_FAIL);
        }
        //empty yaml
        if (StringUtils.isEmpty(content)) {
            throw new FileOperateException("upload file is empty!", ResponseConsts.RET_FILE_EMPTY);
        }
        //Verify whether there exists a configuration namespace
        if (!content.contains("namespace")) {
            content = UploadFileUtil.addNameSpace(content);
        } else {
            //replace namespace content
            content = UploadFileUtil.replaceContent(content);
        }
        return content;
    }

    /**
     * Verify that the file conforms to yaml format.
     *
     * @param content uploaded file content
     * @return
     */
    public static List<Map<String, Object>> verifyYamlFormat(String content) {
        String[] multiContent = content.split("---");
        List<Map<String, Object>> mapList = new ArrayList<>();
        try {
            for (String str : multiContent) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                Yaml yaml = new Yaml(new SafeConstructor());
                Map<String, Object> loaded = yaml.load(str);
                mapList.add(loaded);
            }
        } catch (Exception e) {
            LOGGER.error("failed to validate yaml scheme {}", e.getMessage());
            throw new FileOperateException("failed to validate yaml scheme!", ResponseConsts.RET_FILE_FORMAT_ERROR);
        }
        return mapList;
    }

    /**
     * verifyHelmTemplate.
     *
     * @param mapList file content list
     * @param requiredItems Collection of things to be verified
     */
    public static void verifyHelmTemplate(List<Map<String, Object>> mapList, List<String> requiredItems) {
        for (Map<String, Object> stringMap : mapList) {
            for (Map.Entry<String, Object> entry : stringMap.entrySet()) {
                if ("kind".equals(entry.getKey())) {
                    if ("Service".equalsIgnoreCase(stringMap.get(entry.getKey()).toString())) {
                        requiredItems.remove("service");
                        continue;
                    }
                    if (stringMap.get("spec") != null) {
                        String specContent = stringMap.get("spec").toString();
                        if (specContent.contains("image")) {
                            requiredItems.remove("image");
                        }
                        if (specContent.contains("mep-agent")) {
                            requiredItems.remove("mep-agent");
                        }
                    }
                }
            }
        }
    }

    /**
     * writeContentToFile.
     *
     * @param content file content
     */
    public static String writeContentToFile(String content) {
        FileWriter writer;
        String fileId = UUID.randomUUID().toString();
        try {
            String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath();
            String fileRealPath = upLoadDir + fileId;
            File dir = new File(upLoadDir);
            if (!dir.isDirectory()) {
                boolean isSuccess = dir.mkdirs();
                if (!isSuccess) {
                    throw new FileOperateException("create upload dir fail!", ResponseConsts.RET_CREATE_FILE_FAIL);
                }
            }
            writer = new FileWriter(fileRealPath);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new FileOperateException("wite upload file failed!", ResponseConsts.RET_WRITE_FILE_FAIL);
        }
        return fileId;
    }

}
