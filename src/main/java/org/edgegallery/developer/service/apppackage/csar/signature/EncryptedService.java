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

package org.edgegallery.developer.service.apppackage.csar.signature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedService.class);

    @Value("${signature.encrypted-key-path:}")
    private String keyPath;

    @Value("${signature.key-password:}")
    private String keyPasswd;

    public boolean encryptedFile(String filePath) {
        BufferedReader reader = null;
        try {
            if (filePath == null) {
                LOGGER.error("Failed to encrypted code.");
                return false;
            }
            File mfFile = getMfFile(filePath);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(mfFile), StandardCharsets.UTF_8));
            reader = new BufferedReader(br);
            String tempString = null;
            String sha256String = null;
            StringBuilder bf = new StringBuilder();
            while ((tempString = reader.readLine()) != null) {
                tempString.trim();
                if (tempString.startsWith("Source")) {
                    String tempPath = tempString.substring(8).trim();
                    String path = filePath + File.separator + tempPath;
                    String encryptedFilePath = path.replace("\\", File.separator).replace("/", File.separator);
                    encryptedFilePath = encryptedFilePath.replace(" ", "");
                    File file = new File(encryptedFilePath);
                    sha256String = getHash(file);
                    bf.append(tempString).append("\r\n");
                    continue;
                }
                if (tempString.startsWith("Hash")) {
                    String str = "Hash:" + sha256String;
                    bf.append(str).append("\r\n");
                    sha256String = null;
                    continue;
                }
                if (tempString.equals("-----BEGIN CMS-----")) {
                    break;
                }
                bf.append(tempString).append("\r\n");
            }
            br.close();
            try (BufferedWriter out = new BufferedWriter(new FileWriter(mfFile));) {
                out.write(bf.toString());
                out.flush();
            }
        } catch (IOException e) {
            LOGGER.error("Hash package failed.");
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("close stream failed.");
                }
            }
        }
        return true;
    }

    public boolean encryptedCMS(String filePath) {
        boolean encryptedFile = encryptedFile(filePath);
        if (!encryptedFile) {
            LOGGER.error("Hash package failed.");
            return false;
        }
        BufferedReader reader = null;
        BufferedWriter out = null;
        try {
            if (filePath == null) {
                throw new IOException("Failed to encrypted code.");
            }
            File mfFile = getMfFile(filePath);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(mfFile), StandardCharsets.UTF_8));
            reader = new BufferedReader(br);
            String tempString = null;
            StringBuilder bf = new StringBuilder();
            while ((tempString = reader.readLine()) != null) {
                bf.append(tempString).append("\r\n");
            }
            br.close();

            String encrypted = signPackage(mfFile.getCanonicalPath(), keyPasswd);
            out = new BufferedWriter(new FileWriter(mfFile));
            out.write(bf.toString());
            out.write("-----BEGIN CMS-----");
            out.write("\n");
            out.write(encrypted);
            out.write("\n");
            out.write("-----END CMS-----");
            out.flush();
            out.close();
            if (StringUtils.isEmpty(encrypted)) {
                LOGGER.error("sign package failed");
                return false;
            }

        } catch (IOException e) {
            LOGGER.error("Failed to encrypted code.");
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("close stream failed.");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error("close stream failed.");
                }
            }
        }
        return true;
    }

    private File getMfFile(String filePath) {
        ArrayList<String> files = new ArrayList<>();
        File file = new File(filePath);
        File mfFile = null;
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            files.add(fileList[i].toString());
            String fileName = fileList[i].getName();
            if (fileName.contains(".mf")) {
                mfFile = fileList[i];
            }
        }
        return mfFile;
    }

    private static String getHash(File file) {
        try (FileInputStream fis = new FileInputStream(file.getCanonicalPath())) {
            return DigestUtils.sha256Hex(fis);
        } catch (IOException e) {
            LOGGER.error("get hash value of source file failed! {}", file.getPath());
            throw new FileOperateException("get hash value of source file failed!", ResponseConsts.RET_HASH_FILE_FAIL);
        }

    }

    private String signPackage(String filePath, String keyPasswd) {
        if (!"".equals(keyPath)) {
            if (filePath != null && !"".equals(filePath)) {
                List<String> rules = new ArrayList<>();
                rules.add("[Ss]ource\\s*:");
                rules.add("[Aa]lgorithm\\s*:");
                rules.add("[Hh]ash\\s*:");
                String in = readMatchLineContent(filePath, rules);
                if (StringUtils.isEmpty(in)) {
                    return "";
                }
                Signature signature = new Signature();
                Optional<byte[]> signBytes = signature
                    .signMessage(in.trim(), StandardCharsets.UTF_8.toString(), keyPath, keyPasswd);
                if (signBytes.isPresent()) {
                    return new String(signBytes.get(), StandardCharsets.UTF_8);
                } else {
                    LOGGER.error("sign package failed");
                    return "";
                }
            } else {
                LOGGER.error("sign package failed");
                return "";
            }
        } else {
            LOGGER.error("sign package failed");
            return "";
        }
    }

    private static boolean canLineMatch(String line, List<String> rules) {
        String normalizeLine = Normalizer.normalize(line, Normalizer.Form.NFKC);

        for (String rule : rules) {
            if (normalizeLine.matches(rule) || normalizeLine.split(rule).length > 1) {
                return true;
            }
        }
        return false;
    }

    private static String readMatchLineContent(String fileName, List<String> rules) {
        StringBuilder result = new StringBuilder();
        String filePath = fileName.replace("\\", File.separator).replace("/", File.separator);
        try (InputStream inputStream = FileUtils.openInputStream(FileUtils.getFile(filePath));
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
             LineIterator lineIterator = new LineIterator(bufferedReader)) {
            String line;

            while (lineIterator.hasNext()) {
                line = lineIterator.next();
                if (line == null || "".equals(line) || canLineMatch(line, rules)) {
                    line += "\n";
                    result.append(line);
                }
            }
        } catch (IOException e) {
            LOGGER.error("sign package failed");
            return null;
        }
        return result.toString().trim();
    }
}