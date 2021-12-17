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

package org.edgegallery.developer.service.plugin.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.model.plugin.AFile;
import org.edgegallery.developer.service.plugin.PluginFileService;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.filechecker.FileChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class PluginFileServiceImpl implements PluginFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginFileServiceImpl.class);

    private static final String PLUGIN_DIR = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getPluginPath();

    private static final String PACKAGE_XML_FORMAT = ".xml";

    private static final String PACKAGE_YAML_FORMAT = ".yaml";

    private static final String PACKAGE_CSH_FORMAT = ".csh";

    private static final String PACKAGE_META_FORMAT = ".meta";

    private static final String PACKAGE_TXT_FORMAT = ".txt";

    private static final String MANIFEST = ".mf";

    private static final String MARKDOWN = ".md";

    private static final List<String> FILE_TYPE_LIST = Arrays
        .asList(PACKAGE_XML_FORMAT, PACKAGE_YAML_FORMAT, PACKAGE_CSH_FORMAT, PACKAGE_META_FORMAT, PACKAGE_TXT_FORMAT,
            MANIFEST, MARKDOWN);

    private String generateFileName() {
        String random = UUID.randomUUID().toString();
        return random.replace("-", "");
    }

    @Override
    public String saveTo(MultipartFile file, FileChecker fileChecker) throws IOException {
        if (file == null || StringUtils.isEmpty(file.getOriginalFilename())) {
            throw new IllegalArgumentException("file is null");
        }
        fileChecker.check(file);
        String fileName = generateFileName();
        String originalFileName = file.getOriginalFilename();
        StringBuilder sb = new StringBuilder();
        sb.append(PLUGIN_DIR).append(fileName).append(originalFileName);
        String fileAddress = sb.toString();
        if (!new File(PLUGIN_DIR).exists() && !new File(PLUGIN_DIR).mkdirs()) {
            throw new IllegalArgumentException("mkdir error");
        }
        file.transferTo(new File(fileAddress));
        fileChecker.check(new File(fileAddress));
        return fileAddress;
    }

    public InputStream get(AFile afile) throws IOException {
        return new BufferedInputStream(FileUtils.openInputStream(new File(afile.getStorageAddress())));
    }

    @Override
    public String get(String fileAddress, String filePath) throws IOException {
        return getCsarFileContentByName(fileAddress, filePath);
    }

    @Override
    public void delete(AFile afile) {
        try {
            java.nio.file.Files.deleteIfExists(Paths.get(afile.getStorageAddress()));
        } catch (IOException e) {
            LOGGER.error("delete file error {}", e.getMessage());
        }
    }

    /**
     * get file content by file path and file.
     *
     * @param filePath csar file address.
     * @param file file path in package.
     * @return
     */
    private static String getCsarFileContentByName(String filePath, String file) throws IOException {
        File inputFile = new File(file.toLowerCase());
        String type = inputFile.getName();
        if (type.contains(".")) {
            type = type.substring(type.lastIndexOf("."));
        }
        if (!FILE_TYPE_LIST.contains(type)) {
            LOGGER.error("file type {} error", type);
            throw new IllegalRequestException("file type error", ResponseConsts.RET_REQUEST_PARAM_ERROR);
        }
        return readFileContent(filePath, file);
    }

    private static String readFileContent(String filePath, String file) throws IOException {
        String target = file.replace(":", File.separator);
        try (ZipFile zipFile = new ZipFile(filePath); StringWriter writer = new StringWriter()) {
            ZipEntry result = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equals(target)) {
                    result = entry;
                    break;
                }
            }
            if (result == null) {
                LOGGER.error("file {} not found", file);
                throw new FileFoundFailException("file not found", ResponseConsts.RET_FILE_NOT_FOUND);
            }
            try (InputStream inputStream = zipFile.getInputStream(result)) {
                IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
            }
            return writer.toString();
        }
    }
}
