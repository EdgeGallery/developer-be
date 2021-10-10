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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeveloperFileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeveloperFileUtils.class);

    private static final int BUFFER_LENGTH = 8192;

    private DeveloperFileUtils() {
    }

    /**
     * get Absolute Path.
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
    public static File copyDirAndReName(File srcDir, File desDir, String name) throws IOException {
        FileUtils.copyDirectoryToDirectory(srcDir, desDir);
        File src = new File(desDir, srcDir.getName());
        File res = new File(desDir, name);
        if (res.exists() && res.isDirectory()) {
            FileUtils.deleteDirectory(res);
        }
        if (src.renameTo(res)) {
            return res;
        }
        throw new IOException("copy template exception");
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
     * createFileItem.
     *
     * @return
     */
    public static FileItem createFileItem(File file, String fieldName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem(fieldName, "text/plain", true, file.getName());
        int bytesRead = 0;
        byte[] buffer = new byte[BUFFER_LENGTH];
        try (FileInputStream fis = FileUtils.openInputStream(file); OutputStream os = item.getOutputStream()) {
            while ((bytesRead = fis.read(buffer, 0, BUFFER_LENGTH)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            LOGGER.error("file to fileItem occur exception {}", e.getMessage());
        }
        return item;
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
     *
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
     * moveFile.
     */
    public static void moveFile(File res, File des) throws IOException {
        FileUtils.moveFile(res, des);
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
     * deleteAndCreateFile.
     */
    public static void deleteAndCreateFile(File fi) throws IOException {
        if (fi.exists() && fi.isFile()) {
            FileUtils.deleteQuietly(fi);
        }
        boolean isCreated = fi.createNewFile();
        if (!isCreated) {
            throw new IOException("Create file error.");
        }
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
