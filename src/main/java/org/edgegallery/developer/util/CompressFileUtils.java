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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
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
     * decompress .zip or .csar or .targz
     *
     * @param filePath
     * @param outputDir
     */
    public static boolean decompress(String filePath, String outputDir) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("decompress file not exist.");
            return false;
        }
        try {
            if (filePath.endsWith(".zip") || filePath.endsWith(".csar") ) {
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
            e.printStackTrace();
            LOGGER.error("Failed to decompress file:{}.", filePath);
        }
        return false;
    }

    /**
     * decompress .zip or .csar
     *
     * @param file
     * @param outputDir
     * @throws IOException
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
     * decompress tar.gz
     *
     * @param file
     * @param outputDir
     */
    public static void decompressTarGz(File file, String outputDir) throws IOException {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
                new GzipCompressorInputStream(
                        new BufferedInputStream(
                                new FileInputStream(file))))) {
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
                    if (!temp.exists()){
                        temp.createNewFile();
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
     * decompress tar.bz2
     *
     * @param file
     * @param outputDir
     */
    public static void decompressTarBz2(File file, String outputDir) throws IOException {
        try (TarArchiveInputStream tarIn =
                     new TarArchiveInputStream(
                             new BZip2CompressorInputStream(
                                     new FileInputStream(file)))) {
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
     * create directory
     *
     * @param outputDir
     * @param subDir
     */
    public static void createDirectory(String outputDir, String subDir) {
        File file = new File(outputDir);
        // if subDir exists
        if (!(subDir == null || "".equals(subDir.trim()))) {
            file = new File(outputDir + File.separator + subDir);
        }
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.mkdirs();
        }
    }

    /**
     * writeFile
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void writeFile(InputStream in, OutputStream out) throws IOException {
        int length;
        byte[] b = new byte[BUFFER];
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
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
