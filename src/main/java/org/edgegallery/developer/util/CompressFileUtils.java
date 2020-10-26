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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompressFileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressFileUtils.class);

    private static final int BUFFER = 1048576;


    private CompressFileUtils() {
    }

    /**
     * compressToTgzAndDeleteSrc.
     *
     * @return
     */
    public static File compressToTgzAndDeleteSrc(String sourcePath, String outPutPath, String fileName)
        throws IOException {
        File res = compressToTgz(sourcePath, outPutPath, fileName);
        FileUtils.deleteDirectory(new File(sourcePath));
        return res;
    }

    /**
     * compressToCsarAndDeleteSrc.
     *
     * @return
     */
    public static File compressToCsarAndDeleteSrc(String sourcePath, String outPutPath, String fileName)
        throws IOException {
        File res = compressToCsar(sourcePath, outPutPath, fileName);
        if (res == null) {
            LOGGER.error("Failed to compress file.");
            throw new IOException("Failed to compress file.");
        }
        FileUtils.deleteDirectory(new File(sourcePath));
        return res;
    }

    /**
     * compressToTgz.
     *
     * @return
     */
    private static File compressToTgz(String sourcePath, String outPutPath, String fileName) throws IOException {
        File tar = pack(sourcePath);
        File targetFile = new File(outPutPath);
        if (!targetFile.exists()) {
            boolean isSuccess = targetFile.mkdirs();
            if (!isSuccess) {
                return null;
            }
        }
        File outPutFile = new File(outPutPath + File.separator + fileName + ".tgz");
        try (BufferedInputStream bis = new BufferedInputStream(FileUtils.openInputStream(tar), BUFFER);
             FileOutputStream fo = new FileOutputStream(outPutFile);
             BufferedOutputStream bufOut = new BufferedOutputStream(fo);
             GZIPOutputStream gzp = new GZIPOutputStream(bufOut)) {
            int count = 0;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                gzp.write(data, 0, count);
            }
        } finally {
            if (tar.exists()) {
                FileUtils.deleteQuietly(tar);
            }
        }
        return outPutFile;
    }

    private static File pack(String sourcePath) throws IOException {
        File srcFile = new File(sourcePath);
        String name = srcFile.getName();
        String basePath = srcFile.getParent();
        File target = new File(basePath + File.separator + name + ".tar");
        try (TarArchiveOutputStream taos = new TarArchiveOutputStream(FileUtils.openOutputStream(target))) {
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            archive(srcFile, taos, "");
            taos.flush();
        }
        return target;
    }

    private static void archive(File srcFile, TarArchiveOutputStream taos, String basePath) throws IOException {
        if (srcFile.isDirectory()) {
            archiveDir(srcFile, taos, basePath);
        } else {
            archiveFile(srcFile, taos, basePath);
        }
    }

    private static void archiveFile(File file, TarArchiveOutputStream taos, String dir) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(dir + file.getName());
        entry.setSize(file.length());
        taos.putArchiveEntry(entry);
        try (BufferedInputStream bis = new BufferedInputStream(FileUtils.openInputStream(file));) {
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data)) != -1) {
                taos.write(data, 0, count);
            }
            taos.closeArchiveEntry();
        }
    }

    private static void archiveDir(File dir, TarArchiveOutputStream taos, String basePath) throws IOException {
        File[] files = dir.listFiles();
        if (files == null || files.length < 1) {
            TarArchiveEntry entry = new TarArchiveEntry(basePath + dir.getName() + File.separator);
            taos.putArchiveEntry(entry);
            taos.closeArchiveEntry();
        } else {
            for (File file : files) {
                archive(file, taos, basePath + dir.getName() + File.separator);
            }
        }
    }

    private static File compressToCsar(String resourcesPath, String targetPath, String fileName) throws IOException {
//        Path path = Paths.get(targetPath + File.separator + fileName + ".csar");
//        Files.deleteIfExists(path);
        ZipFile zipFile = new ZipFile(targetPath + File.separator + fileName + ".csar");
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);

        zipFile.addFolder(new File(resourcesPath), parameters);
        File csar = new File(targetPath + File.separator + fileName + ".csar");
        if (csar.exists()) {
            return csar;
        } else {
            return null;
        }
    }

}
