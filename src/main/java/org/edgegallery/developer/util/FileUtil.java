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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public final class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final List<String> ICON_LIST = Arrays.asList("png", "jpg");

    private static final List<String> MD_LIST = Arrays.asList("md", "MD");

    private static final List<String> API_LIST = Arrays.asList("yaml", "yml", "json");

    private static final List<String> SCRIPT_LIST = Arrays.asList("sh");

    private static final Map<String, List<String>> FILE_TYPE_MAP = new HashMap();

    static {
        FILE_TYPE_MAP.put("icon", ICON_LIST);
        FILE_TYPE_MAP.put("md", MD_LIST);
        FILE_TYPE_MAP.put("api", API_LIST);
        FILE_TYPE_MAP.put(Consts.FILE_TYPE_SCRIPT, SCRIPT_LIST);
    }

    private FileUtil() {

    }

    /**
     * Read file content<br>
     *
     * @param filePath file path
     * @return file content
     */
    public static String readFileContent(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return new String(Files.readAllBytes(path), "UTF-8");
        } catch (IOException ex) {
            LOGGER.error("read file {} occur exception {}", filePath, ex.getMessage());
            return "error";
        }
    }

    /**
     * Get all files in the directory.<br>
     *
     * @param dir file directory
     * @return all files in the directory
     */
    public static List<String> getAllFilePath(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        List<String> listLocal = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(dir.toPath())) {
            List<Path> allPaths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path path : allPaths) {
                try {
                    listLocal.add(path.toFile().getCanonicalPath());
                } catch (IOException e) {
                    LOGGER.error("getCanonicalPath {} occur exception {}", path, e.getMessage());
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Files.walk dir {} occur exception {}", dir, ex.getMessage());
        }

        return listLocal;
    }

    /**
     * checkFileSize.
     *
     * @param len file length
     * @param size file size
     * @param unit convert
     * @return
     */
    public static boolean checkFileSize(Long len, int size, String unit) {
        double fileSize = 0;
        if ("B".equals(unit.toUpperCase())) {
            fileSize = (double) len;
        } else if ("K".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1024;
        } else if ("M".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1048576;
        } else if ("G".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1073741824;
        }
        if (fileSize > size) {
            return false;
        }
        return true;
    }

    /**
     * check file type.
     *
     * @param fileName fileName
     * @param fileType fileType
     * @return
     */
    public static boolean checkFileType(String fileName, String fileType) {
        if (!FileChecker.isValid(fileName)) {
            LOGGER.error("icon fileName is invalid.");
            return false;
        }
        String fileNameSuffix = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileNameSuffix = fileName.substring(i + 1);
        }
        List<String> fileSuffixList = FILE_TYPE_MAP.get(fileType);
        if (CollectionUtils.isEmpty(fileSuffixList)) {
            LOGGER.error("fileSuffixList is empty.");
            return false;
        }
        if (!fileSuffixList.contains(fileNameSuffix)) {
            LOGGER.error("file type is error.");
            return false;
        }
        return true;
    }

    /**
     * copy file and return new file.
     *
     * @param source source file
     * @return
     */
    public static File copyFile(File source, String fileName) {
        if (!source.exists()) {
            LOGGER.error("source file does not exist!");
            return null;
        }
        File dest = new File(InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + fileName);
        if (!dest.exists()) {
            try {
                boolean ret = dest.createNewFile();
                if (!ret) {
                    LOGGER.error("create dest file failed!");
                    return null;
                }
                FileUtils.copyFile(source, dest);
            } catch (IOException e) {
                LOGGER.error("create dest file occur {}", e.getMessage());
                return null;
            }
        }
        return dest;
    }
}
