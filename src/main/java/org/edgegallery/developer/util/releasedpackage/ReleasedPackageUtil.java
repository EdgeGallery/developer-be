/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.util.releasedpackage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.util.CompressFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleasedPackageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleasedPackageUtil.class);

    private ReleasedPackageUtil() {
        throw new IllegalStateException("ReleasedPackageUtil class");
    }

    /**
     * decompress app pkg.
     *
     * @param appPkgDir package directory
     * @param outPutPath decompress directory
     * @return if decompress return true or return false
     */
    public static boolean decompressAppPkg(String appPkgDir, String outPutPath) {
        File file = new File(outPutPath);
        if (!file.exists() && !file.mkdirs()) {
            LOGGER.error("create dir {} failed!", outPutPath);
            return false;
        }
        return CompressFileUtils.decompress(appPkgDir, outPutPath);
    }

    /**
     * get decompressPkgDir catalog.
     *
     * @param decompressPkgDir package decompress directory
     * @return if decompressPkgDir exist return file list or return empty list
     */
    public static List<AppPkgFile> getCatalogue(String decompressPkgDir) {
        if (StringUtils.isEmpty(decompressPkgDir)) {
            return Collections.emptyList();
        }
        String appPkgRootDir = decompressPkgDir.replaceAll("\\\\", "/").replaceAll("//", "/");
        LOGGER.info("appPkgRootDir:{}", appPkgRootDir);
        File root = new File(decompressPkgDir);
        try {
            return deepReadDir(new ArrayList<>(), root, decompressPkgDir, appPkgRootDir);
        } catch (IOException e) {
            LOGGER.error("Failed to get catalog. maybe read file error.");
            return Collections.emptyList();
        }
    }

    private static List<AppPkgFile> deepReadDir(List<AppPkgFile> files, File root, String decompressPkgDir,
        String appPkgRootDir) throws IOException {
        LOGGER.info("appPkgRootDir:{}", appPkgRootDir);
        if (root.isFile()) {
            AppPkgFile file = AppPkgFile.builder().fileName(root.getName()).isFile(true)
                .filePath(root.getPath().replace(decompressPkgDir, "")).build();
            // when the size of file is over 1mb, will not read content to memory.
            if (root.length() < 1024 * 1024) {
                file.setContent(FileUtils.readFileToString(Paths.get(decompressPkgDir, file.getFilePath()).toFile(),
                    StandardCharsets.UTF_8));
            }
            String filePath = root.getPath().replaceAll("\\\\", "/");
            LOGGER.info("root file:{}", filePath);
            file.setFilePath(filePath.replace(appPkgRootDir, ""));
            files.add(file);
        }

        if (root.isDirectory()) {
            AppPkgFile file = AppPkgFile.builder().fileName(root.getName()).isFile(false)
                .filePath(root.getPath().replace(decompressPkgDir, "")).build();
            List<AppPkgFile> children = new ArrayList<>();
            file.setChildren(children);
            String filePath = root.getPath().replaceAll("\\\\", "/");
            file.setFilePath(filePath.replace(appPkgRootDir, ""));
            files.add(file);
            for (File childrenFile : Objects.requireNonNull(root.listFiles())) {
                deepReadDir(children, childrenFile, childrenFile.getParent(), appPkgRootDir);
            }
        }
        return files;
    }

    /**
     * get file content.
     *
     * @param filePath file path in app package
     * @param decompressPkgDir package decompress directory
     * @return if success return content or null
     */
    public static String getContentByInnerPath(String filePath, String decompressPkgDir) {
        try {
            String pkgDir = decompressPkgDir.replaceAll("\\\\", "/");
            return FileUtils.readFileToString(Paths.get(pkgDir, filePath).toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to read the inner file. innerPath:{}", filePath);
            return null;
        }
    }

    /**
     * modify file.
     *
     * @param filePath file path in app package
     * @param content new file content
     * @param decompressPkgDir package decompress directory
     * @return if success return true or false
     */
    public static boolean modifyFileByPath(String filePath, String content, String decompressPkgDir) {
        try {
            String pkgDir = decompressPkgDir.replaceAll("\\\\", "/");
            Path realPath = Paths.get(pkgDir, filePath);
            if (realPath.toFile().exists() && realPath.toFile().isFile()) {
                Files.write(realPath, content.getBytes(StandardCharsets.UTF_8));
                return true;
            }
            LOGGER.warn("Can not find file by the innerPath: {}", filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to modify the innerFile. innerPath:{}", filePath);
        }
        return false;
    }

    /**
     * get files.
     *
     * @param filePath file Path
     * @return if success return file list or return empty list
     */
    public static List<File> getFiles(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            LOGGER.info("directory {} not exist", filePath);
            return Collections.emptyList();
        }
        return Arrays.stream(file.listFiles()).filter(item -> item.getName().endsWith("tgz"))
            .collect(Collectors.toList());
    }
}
