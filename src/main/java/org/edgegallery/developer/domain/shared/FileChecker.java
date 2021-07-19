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

package org.edgegallery.developer.domain.shared;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public abstract class FileChecker {

    protected static final String WORK_TEMP_DIR = "\\home\\appstore";

    private static final Pattern PATTERN = Pattern
        .compile("[^\\s\\\\/:*?\"<>|](\\x20|[^\\s\\\\/:*?\"<>|])" + "*[^\\s\\\\/:*?\"<>|.]$");

    private static final int MAX_LENGTH_FILE_NAME = 255;

    protected abstract long getMaxFileSize();

    /**
     * check file if is invalid.
     *
     * @param file object.
     */
    public void check(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null && !isAllowedFileName(originalFilename)) {
            throw new IllegalArgumentException(originalFilename + " :fileName is Illegal");
        }

        if (file.getSize() > getMaxFileSize()) {
            throw new IllegalArgumentException(originalFilename + " :fileSize is Illegal");
        }
    }

    public void check(File file) throws IOException {

    }

    private boolean isAllowedFileName(String originalFilename) {
        if (isValid(originalFilename)) {
            return true;
        }
        if (originalFilename.endsWith("md") || originalFilename.endsWith("MD") || originalFilename.endsWith("mD")
            || originalFilename.endsWith("Md")) {
            return true;
        }
        return false;
    }

    /**
     * to check the filename.
     */
    public static boolean isValid(String fileName) {
        if (StringUtils.isEmpty(fileName) || fileName.length() > MAX_LENGTH_FILE_NAME) {
            return false;
        }
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        return PATTERN.matcher(fileName).matches();
    }
}



