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

package org.edgegallery.developer.util.filechecker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;



public class PluginChecker extends FileChecker {
    @Override
    protected long getMaxFileSize() {
        return 52428800;
    }

    @Override
    protected List<String> getFileExtensions() {
        return Arrays.asList("csar", "zip");
    }

    @Override
    public void check(MultipartFile file) {
        super.check(file);

        String originalFileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFileName)) {
            throw new IllegalArgumentException("package name is Illegal");
        }
    }

    @Override
    public void check(File file) throws IOException {
        super.check(file);
        unzip(file);
    }

    static final int BUFFER = 512;

    static final int TOOBIG = 0x6400000; // max size of unzipped data, 100MB

    static final int TOOMANY = 1024; // max number of files

    // ...

    private String sanitzeFileName(String entryName, String intendedDir) throws IOException {

        File f = new File(intendedDir, entryName);
        String canonicalPath = f.getCanonicalPath();
        File intendDir = new File(intendedDir);
        String canonicalID = intendDir.getCanonicalPath();
        if (canonicalPath.startsWith(canonicalID)) {
            return canonicalPath;
        } else {
            throw new IllegalStateException("File is outside extraction target directory.");
        }
    }

    /**
     * Prevent bomb attacks.
     *
     * @param file file.
     * @throws IOException throw IOException
     */
    public final void unzip(File file) throws IOException {
        FileInputStream fis = FileUtils.openInputStream(file);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        int entries = 0;
        int total = 0;
        byte[] data = new byte[BUFFER];
        try {
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                // Write the files to the disk, but ensure that the entryName is valid,
                // and that the file is not insanely big
                String name = sanitzeFileName(entry.getName(), WORK_TEMP_DIR);
                File f = new File(name);
                if (isDir(entry, f)) {
                    continue;
                }
                FileOutputStream fos = FileUtils.openOutputStream(f);
                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                    while (total <= TOOBIG && (count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                        total += count;
                    }
                    dest.flush();
                }
                zis.closeEntry();
                entries++;
                if (entries > TOOMANY) {
                    throw new IllegalStateException("Too many files to unzip.");
                }
                if (total > TOOBIG) {
                    throw new IllegalStateException("File being unzipped is too big.");
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException();
        } finally {
            zis.close();
            FileUtils.deleteQuietly(new File(WORK_TEMP_DIR));
        }
    }

    /**
     * check if entry is directory, if then create dir.
     *
     * @param entry entry of next element.
     * @param f File
     * @return
     */
    private boolean isDir(ZipEntry entry, File f) {
        if (entry.isDirectory()) {
            boolean isSuccess = f.mkdirs();
            if (isSuccess) {
                return true;
            }
        }
        return false;
    }
}
