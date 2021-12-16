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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeveloperFileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeveloperFileUtils.class);

    private DeveloperFileUtils() {
    }

    /**
     * get Absolute Path
     *
     * @throws IOException io exception
     */

    public static String getAbsolutePath(String relativePath) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + relativePath
            + File.separator;
    }

    /**
     * copy template to des dir and rename the dir name.
     *
     * @param srcDir src
     * @param desDir des
     * @param name new name
     * @return new file
     * @throws IOException io exception
     */
    public static File copyDirectory(File srcDir, File desDir, String name) throws IOException {
        File res = new File(desDir, name);
        if (res.exists() && res.isDirectory()) {
            FileUtils.forceDelete(res);
        }
        FileUtils.copyDirectory(srcDir, res);
        return res;
    }

    /**
     * delteTempFile.
     */
    public static void deleteTempFile(File file) {
        if (file != null) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to delete temp file {}", file.getName());
            }
        }
    }

    /**
     * deleteDir.
     *
     * @return
     */
    public static void deleteDir(String dir) {
        File f = new File(dir);
        try {
            FileUtils.deleteDirectory(f);
        } catch (IOException e) {
            LOGGER.error("Delete file directory failed: {}", e.getMessage());
        }
    }

    /**
     * deleteAndCreateDir.
     */
    public static void deleteAndCreateDir(String dir) throws IOException {
        Path path = Paths.get(dir);
        Files.deleteIfExists(path);
        Files.createDirectories(path);
    }

    /**
     * deleteAndCreateDir.
     */
    public static void deleteAndCreateDir(File dir) throws IOException {
        Path path = dir.toPath();
        Files.deleteIfExists(path);
        Files.createDirectories(path);
    }

    /**
     * clear all files in one dir.
     */
    public static void clearFiles(String workspaceRootPath) {
        File file = new File(workspaceRootPath);
        if (file.exists()) {
            deleteFile(file);
        }
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

    /**
     * copyFile.
     */
    public static void copyFile(File res, File des) throws IOException {
        FileUtils.copyFile(res, des);
    }

    /**
     * readFileToString.
     *
     * @return
     */
    public static String readFileToString(File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    /**
     * create a template dir.
     *
     * @return temp file
     */
    public static File createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix).toFile();
        } catch (IOException e) {
            LOGGER.error("create template file error, please check the disk.  msg={}", e.getMessage());
        }
        return null;
    }
}
