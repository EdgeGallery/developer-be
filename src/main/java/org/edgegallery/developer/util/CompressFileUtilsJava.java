package org.edgegallery.developer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressFileUtilsJava {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompressFileUtilsJava.class);

    private static final int BUFFER = 1048576;

    private CompressFileUtilsJava() {
        throw new IllegalStateException("CompressFileUtilsJava class");
    }

    /**
     * compressToTgzAndDeleteSrc.
     */
    public static File compressToTgzAndDeleteSrc(String sourcePath, String outPutPath, String fileName)
        throws IOException {
        File res = compressToTgz(sourcePath, outPutPath, fileName);
        FileUtils.deleteDirectory(new File(sourcePath));
        return res;
    }

    public static void zipFiles(List<File> srcfile, File zipfile) {
        List<String> entryPaths = new ArrayList<>();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));) {
            for (File file : srcfile) {
                if (file.isFile()) {
                    addFileToZip(out, file, entryPaths);
                } else if (file.isDirectory()) {
                    entryPaths.add(file.getName());
                    addFolderToZip(out, file, entryPaths);
                    entryPaths.remove(entryPaths.size() - 1);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to generate zip file.", e);
        }
    }
    private static void addFolderToZip(ZipOutputStream out, File file, List<String> entryPaths) throws IOException {
        out.putNextEntry(new ZipEntry(StringUtils.join(entryPaths, "/") + "/"));
        out.closeEntry();
        for (File subFile : file.listFiles()) {
            if (subFile.isFile()) {
                addFileToZip(out, subFile, entryPaths);
            } else if (subFile.isDirectory()) {
                entryPaths.add(subFile.getName());
                addFolderToZip(out, subFile, entryPaths);
                entryPaths.remove(entryPaths.size() - 1);
            }
        }
    }

    private static void addFileToZip(ZipOutputStream out, File file, List<String> entryPaths) throws IOException {
        byte[] buf = new byte[1024];
        try (FileInputStream in = new FileInputStream(file)) {
            if (entryPaths.size() > 0) {
                out.putNextEntry(new ZipEntry(StringUtils.join(entryPaths, "/") + "/" + file.getName()));
            } else {
                out.putNextEntry(new ZipEntry(file.getName()));
            }
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        }
    }

    /**
     * compressToCsarAndDeleteSrc.
     */
    public static File compressToCsarAndDeleteSrc(String sourcePath, String outPutPath, String fileName)
        throws IOException {
        File res = compressToCsar(sourcePath, outPutPath, fileName);
        return res;
    }

    /**
     * compressToTgz.
     */
    public static File compressToTgz(String sourcePath, String outPutPath, String fileName) throws IOException {
        File tar = pack(sourcePath);
        File targetFile = new File(outPutPath);
        if (!targetFile.exists()) {
            boolean isMake = targetFile.mkdirs();
            if (!isMake) {
                LOGGER.error("compressToTgz: make dir failed!");
                return null;
            }
        }
        File outPutFile = new File(outPutPath + File.separator + fileName + ".tgz");
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tar), BUFFER);
             GZIPOutputStream gzp = new GZIPOutputStream(new FileOutputStream(outPutFile));) {
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                gzp.write(data, 0, count);
            }
        } finally {
            if (tar.exists()) {
                boolean isDelete = tar.delete();
                if (!isDelete) {
                    LOGGER.error("compressToTgz: delete tar failed!");
                }
            }
        }
        return outPutFile;
    }

    /**
     * compressToTgz.
     */
    public static File compressToZip(String sourcePath, String outPutPath, String fileName) throws IOException {
        File resourcesFile = new File(sourcePath);
        File targetFile = new File(outPutPath);
        if (!targetFile.exists()) {
            boolean isMaked = targetFile.mkdirs();
            if (!isMaked) {
                LOGGER.error("compressToZip: make dir failed");
                return null;
            }
        }

        FileOutputStream outputStream = new FileOutputStream(outPutPath + File.separator + fileName + ".zip");
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));

        createCompressedFile(out, resourcesFile, "");

        out.close();
        File csar = new File(outPutPath + File.separator + fileName + ".zip");
        if (csar.exists()) {
            return csar;
        }
        throw new IOException("zip not find");
    }

    private static File pack(String sourcePath) throws IOException {
        File srcFile = new File(sourcePath);
        String name = srcFile.getName();
        String basePath = srcFile.getParent();
        File target = new File(basePath + File.separator + name + ".tar");
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new FileOutputStream(target));
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        archive(srcFile, taos, "");
        taos.flush();
        taos.close();
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
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                taos.write(data, 0, count);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("archiveFile occur exception: {}", e.getMessage());
        }
        taos.closeArchiveEntry();
    }

    private static void archiveDir(File dir, TarArchiveOutputStream taos, String basePath) throws IOException {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            if (files.length < 1) {
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

    /**
     * compressToCsar.
     */
    public static File compressToCsar(String resourcesPath, String targetPath, String fileName) throws IOException {
        File resourcesFile = new File(resourcesPath);
        File targetFile = new File(targetPath);
        if (!targetFile.exists()) {
            boolean isMaked = targetFile.mkdirs();
            if (!isMaked) {
                LOGGER.error("compressToCsar: make dir failed");
                return null;
            }
        }

        FileOutputStream outputStream = new FileOutputStream(targetPath + File.separator + fileName + ".csar");
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));

        createCompressedFile(out, resourcesFile, "");

        out.close();
        File csar = new File(targetPath + File.separator + fileName + ".csar");
        if (csar.exists()) {
            return csar;
        }
        throw new IOException("csar not find");
    }

    private static void createCompressedFile(ZipOutputStream out, File file, String dir) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (!dir.equals("")) {
                try {
                    out.putNextEntry(new ZipEntry(dir + "/"));
                } catch (IOException e) {
                    LOGGER.error("createCompressedFile: dir putNextEntry failed, {}", e.getMessage());
                }

            }

            dir = dir.length() == 0 ? "" : dir + "/";
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    createCompressedFile(out, files[i], dir + files[i].getName());
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                out.putNextEntry(new ZipEntry(dir));
                int j = 0;
                byte[] buffer = new byte[1024];
                while ((j = fis.read(buffer)) > 0) {
                    out.write(buffer, 0, j);
                }
            } catch (FileNotFoundException fe) {
                LOGGER.error("createCompressedFile: can not find param file,{}", fe.getMessage());
            } catch (IOException ie) {
                LOGGER.error("createCompressedFile: file putNextEntry failed, {}", ie.getMessage());
            }
        }
    }

}
