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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;

public final class CsarFileUtil {

    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private CsarFileUtil() {
    }

    /**
     * unzip zip file.
     */
    public static List<String> unzip(String zipFileName, String extPlace) throws IOException {
        List<String> unzipFileNams = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipFileName);) {
            Enumeration<?> fileEn = zipFile.entries();
            byte[] buffer = new byte[BUFFER_SIZE];

            while (fileEn.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) fileEn.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                File file = new File(extPlace, entry.getName());
                if (!file.getParentFile().exists()) {
                    createDirectory(file.getParentFile().getCanonicalPath());
                }

                try (InputStream input = zipFile.getInputStream(entry);
                     BufferedOutputStream bos = new BufferedOutputStream(FileUtils.openOutputStream(file));) {
                    int length = 0;
                    if (input != null) {
                        while ((length = input.read(buffer)) != -1) {
                            bos.write(buffer, 0, length);
                        }
                    }
                    unzipFileNams.add(file.getCanonicalPath());
                }
            }
        }
        return unzipFileNams;
    }

    /**
     * create dir.
     */
    public static boolean createDirectory(String dir) {
        File folder = new File(dir);
        if (!folder.isDirectory()) {
            return folder.mkdirs();
        }
        return true;
    }

}
