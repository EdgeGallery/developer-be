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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
     * decompress .zip or .csar or .targz.
     */
    public static boolean decompress(String filePath, String outputDir) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.error("decompress file not exist.");
            return false;
        }
        try {
            if (filePath.endsWith(".zip") || filePath.endsWith(".csar")) {
                unZip(file, outputDir);
            }
            if (filePath.endsWith(".tar.gz") || filePath.endsWith(".tgz")) {
                decompressTarGz(file, outputDir);
            }
            if (filePath.endsWith(".tar.bz2")) {
                decompressTarBz2(file, outputDir);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to decompress file:{}.", filePath);
        }
        return false;
    }

    /**
     * decompress .zip or .csar.
     */
    public static void unZip(File file, String outputDir) throws IOException {
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file, StandardCharsets.UTF_8)) {
            // create out dir
            createDirectory(outputDir, null);
            Enumeration<?> enums = zipFile.entries();
            while (enums.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enums.nextElement();
                if (entry.isDirectory()) {
                    // create empty dir
                    createDirectory(outputDir, entry.getName());
                } else {
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        try (OutputStream out = new FileOutputStream(
                            new File(outputDir + File.separator + entry.getName()))) {
                            writeFile(in, out);
                        }
                    }
                }
            }
        }
    }

    /**
     * decompress tar.gz.
     */
    public static void decompressTarGz(File file, String outputDir) throws IOException {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
            new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))))) {
            // create out directory
            createDirectory(outputDir, null);
            TarArchiveEntry entry = null;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                // check is dir
                if (entry.isDirectory()) {
                    createDirectory(outputDir, entry.getName());
                } else {
                    // check is file
                    File temp = new File(outputDir + File.separator + entry.getName());
                    // create dir for file
                    createDirectory(temp.getParent(), null);
                    if (!temp.exists()) {
                        boolean isCreate = temp.createNewFile();
                        if (!isCreate) {
                            return;
                        }
                    }
                    try (OutputStream out = new FileOutputStream(
                        new File(outputDir + File.separator + entry.getName()))) {
                        writeFile(tarIn, out);
                    }
                }
            }
        }
    }

    /**
     * decompress tar.bz2.
     */
    public static void decompressTarBz2(File file, String outputDir) throws IOException {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
            new BZip2CompressorInputStream(new FileInputStream(file)))) {
            createDirectory(outputDir, null);
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    createDirectory(outputDir, entry.getName());
                } else {
                    try (OutputStream out = new FileOutputStream(
                        new File(outputDir + File.separator + entry.getName()))) {
                        writeFile(tarIn, out);
                    }
                }
            }
        }
    }

    /**
     * create directory.
     */
    public static void createDirectory(String outputDir, String subDir) {
        File file = new File(outputDir);
        // if subDir exists
        if (!(subDir == null || "".equals(subDir.trim()))) {
            file = new File(outputDir + File.separator + subDir);
        }
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                boolean isMk = file.getParentFile().mkdirs();
                if (!isMk) {
                    LOGGER.error("mk parent dir failed!");
                    return;
                }
            }
            boolean isMk = file.mkdirs();
            if (!isMk) {
                LOGGER.error("mk dir failed!");
            }
        }
    }

    /**
     * writeFile.
     */
    public static void writeFile(InputStream in, OutputStream out) throws IOException {
        int length;
        byte[] b = new byte[BUFFER];
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

    /**
     * writeFile.
     */
    public static void fileToZip(String filePath, String fileName) {
        if (!StringUtils.isEmpty(filePath)) {
            File dir = new File(filePath);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    List<File> subFiles = Arrays.asList(files);
                    if (!CollectionUtils.isEmpty(subFiles)) {
                        CompressFileUtilsJava
                            .zipFiles(subFiles, new File(filePath + File.separator + fileName + ".zip"));
                        for (File subFile : subFiles) {
                            FileUtils.deleteQuietly(subFile);
                        }
                    }
                }
            }

        }
    }

    /**
     * compressToTgz.
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

}
