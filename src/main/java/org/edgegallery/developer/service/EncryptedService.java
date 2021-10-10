package org.edgegallery.developer.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.FileOperateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("encryptedService")
public class EncryptedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedService.class);

    @Value("${signature.encrypted-key-path:}")
    private String keyPath;

    @Value("${signature.key-password:}")
    private String keyPasswd;

    /**
     * encryptedFile.
     */
    public void encryptedFile(String filePath) {
        BufferedReader reader = null;
        try {
            if (filePath == null) {
                throw new IOException("Failed to encrypted code.");
            }
            File mfFile = getMfFile(filePath);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(mfFile), StandardCharsets.UTF_8));
            reader = new BufferedReader(br);
            String tempString = null;
            String sha256String = null;
            StringBuilder bf = new StringBuilder();
            while ((tempString = reader.readLine()) != null) {
                tempString = tempString.trim();
                if (tempString.startsWith("Source")) {
                    String tempPath = tempString.substring(8).trim();
                    String path = filePath + File.separator + tempPath;
                    String encryptedFilePath = path.replace("\\", File.separator).replace("/", File.separator);
                    encryptedFilePath = encryptedFilePath.replace(" ", "");
                    File file = new File(encryptedFilePath);
                    sha256String = getFileSha1(file);
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
            BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(mfFile), StandardCharsets.UTF_8));
            out.write(bf.toString());
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new FileOperateException("Hash package failed.", ResponseConsts.RET_HASH_FILE_FAIL);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("close bufferreader occur exception {}", e.getMessage());
                }
            }
        }
    }

    /**
     * encryptedCMS.
     */
    public void encryptedCms(String filePath) {
        BufferedReader reader = null;
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
            BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(mfFile), StandardCharsets.UTF_8));
            out.write(bf.toString());
            out.write("-----BEGIN CMS-----");
            out.write("\n");
            out.write(encrypted);
            out.write("\n");
            out.write("-----END CMS-----");
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new FileOperateException("Failed to encrypted code.", ResponseConsts.RET_SIGN_FILE_FAIL);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("close bufferreader occur exception {}", e.getMessage());
                }
            }
        }
    }

    private static String getFileSha1(File file) {
        return getHash(file, "SHA-256");
    }

    private File getMfFile(String filePath) {
        File file = new File(filePath);
        File mfFile = null;
        File[] fileList = file.listFiles();
        if (fileList != null && fileList.length > 0) {
            for (int i = 0; i < fileList.length; i++) {
                String fileName = fileList[i].getName();
                if (fileName.contains(".mf")) {
                    mfFile = fileList[i];
                }
            }
        }
        return mfFile;
    }

    private static String getHash(File file, String hashType) {
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            MessageDigest md5 = MessageDigest.getInstance(hashType);
            for (int numRead = 0; (numRead = fis.read(buffer)) > 0; ) {
                md5.update(buffer, 0, numRead);
            }
            return toHexString(md5.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error("get hash occur exception {}", e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("close InputStream occur exception {}", e.getMessage());
                }
            }
        }
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte a : b) {
            sb.append(Integer.toHexString(a & 0xFF));
        }
        return sb.toString();
    }

    private String signPackage(String filePath, String keyPasswd) {
        if (!"".equals(keyPath)) {
            if (filePath != null && !"".equals(filePath)) {
                List<String> rules = new ArrayList<>();
                rules.add("[Ss]ource\\s*:");
                rules.add("[Aa]lgorithm\\s*:");
                rules.add("[Hh]ash\\s*:");
                String in = readMatchLineContent(filePath, rules);
                Signature signature = new Signature();
                Optional<byte[]> signBytes = signature
                    .signMessage(in.trim(), StandardCharsets.UTF_8.toString(), keyPath, keyPasswd);
                if (signBytes.isPresent()) {
                    return new String(signBytes.get(), StandardCharsets.UTF_8);
                } else {
                    throw new FileOperateException("sign package failed.", ResponseConsts.RET_SIGN_FILE_FAIL);
                }
            } else {
                throw new FileOperateException("sign package failed.", ResponseConsts.RET_SIGN_FILE_FAIL);
            }
        } else {
            throw new FileOperateException("sign package failed.", ResponseConsts.RET_SIGN_FILE_FAIL);
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
        LineIterator lineIterator;
        StringBuilder result = new StringBuilder();
        String filePath = fileName.replace("\\", File.separator).replace("/", File.separator);
        try (InputStream inputStream = FileUtils.openInputStream(FileUtils.getFile(filePath));
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            lineIterator = new LineIterator(bufferedReader);
            String line;
            while (lineIterator.hasNext()) {
                line = lineIterator.next();
                if (line == null || "".equals(line) || canLineMatch(line, rules)) {
                    line += "\n";
                    result.append(line);
                }
            }
        } catch (IOException e) {
            throw new FileOperateException("sign package failed.", ResponseConsts.RET_SIGN_FILE_FAIL);
        }
        return result.toString().trim();
    }
}