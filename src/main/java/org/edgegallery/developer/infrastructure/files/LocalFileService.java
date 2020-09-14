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

package org.edgegallery.developer.infrastructure.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.domain.service.FileService;
import org.edgegallery.developer.domain.shared.AFile;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class LocalFileService implements FileService {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    public static final String PLUGIN_DIR = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getPluginPath();

    public static final String PACKAGE_XML_FORMAT = ".xml";

    public static final String PACKAGE_YAML_FORMAT = ".yaml";

    public static final String PACKAGE_CSH_FORMAT = ".csh";

    public static final String PACKAGE_META_FORMAT = ".meta";

    public static final String PACKAGE_TXT_FORMAT = ".txt";

    public static final String MANIFEST = ".mf";

    public static final String MARKDOWN = ".md";

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
            LOGGER.error("delete file error", e.getMessage());
        }
    }

    /**
     * get file content by file path and file.
     *
     * @param filePath csar file address.
     * @param file file path in package.
     * @return
     */
    public static String getCsarFileContentByName(String filePath, String file) throws IOException {
        String type = file.toLowerCase();
        if (!(type.endsWith(MANIFEST) || type.endsWith(MARKDOWN) || type.endsWith(PACKAGE_XML_FORMAT) || type.endsWith(
            PACKAGE_YAML_FORMAT) || type.endsWith(PACKAGE_CSH_FORMAT) || type.endsWith(PACKAGE_META_FORMAT)
            || type.endsWith(PACKAGE_TXT_FORMAT))) {
            LOGGER.error("file type error");
            throw new IllegalArgumentException();
        }
        return readFileContent(filePath, file);
    }

    private static String readFileContent(String filePath, String file) throws IOException {
        String target = file.replace(":", File.separator);
        try (ZipFile zipFile = new ZipFile(filePath);
             StringWriter writer = new StringWriter()) {
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
                throw new FileNotFoundException(file + " not found");
            }
            try (InputStream inputStream = zipFile.getInputStream(result)) {
                IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
            }
            return writer.toString();
        }
    }
}
