package org.edgegallery.developer.service;

import io.netty.util.internal.ObjectUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.edgegallery.developer.exception.WindowException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.*;

@Service("encryptedService")
public class EncryptedService {

    @Value("${signature.encrypted-key-path:}")
    private String keyPath;

    @Value("${signature.key-password:}")
    private String keyPasswd;

    public void encryptedFile(String filePath) throws WindowException, IOException {
        try {
            BufferedReader reader = null;
            if (filePath == null) {
                throw new IOException("Failed to encrypted code.");
            }
            File mfFile = getMfFile(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mfFile), "utf-8"));
            reader = new BufferedReader(br);
            String tempString = null;
            String sha256String = null;
            StringBuffer bf = new StringBuffer();
            while ((tempString = reader.readLine()) != null) {
                tempString.trim();
                if (tempString.startsWith("Source")) {
                    String tempPath = tempString.substring(8);
                    tempPath.trim();
                    String path = filePath + File.separator + tempPath;
                    String encryptedFilePath = path.replace("\\", File.separator).replace("/", File.separator);
                    encryptedFilePath.replace(" ", "");
                    File file = new File(encryptedFilePath);
                    sha256String = getFileSHA1(file);
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

            BufferedWriter out = new BufferedWriter(new FileWriter(mfFile));
            out.write(bf.toString());
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new IOException("Failed to encrypted code.");
        }
    }

    public void encryptedCMS(String filePath) throws WindowException, IOException {
        try {
            BufferedReader reader = null;
            if (filePath == null) {
                throw new IOException("Failed to encrypted code.");
            }
            File mfFile = getMfFile(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mfFile), "utf-8"));
            reader = new BufferedReader(br);
            String tempString = null;
            StringBuffer bf = new StringBuffer();
            while ((tempString = reader.readLine()) != null) {
                bf.append(tempString).append("\r\n");
            }
            br.close();

            String encrypted = signPackage(mfFile.getCanonicalPath(), keyPasswd);
            BufferedWriter out = new BufferedWriter(new FileWriter(mfFile));
            out.write(bf.toString());
            out.write("-----BEGIN CMS-----");
            out.write("\n");
            out.write(encrypted);
            out.write("\n");
            out.write("-----END CMS-----");
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new IOException("Failed to encrypted code.");
        }
    }

    private static String getFileSHA1(File file) {
        String str = "";
        try {
            str = getHash(file, "SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private File getMfFile(String filePath) {
        ArrayList<String> files = new ArrayList<String>();
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

    private static String getHash(File file, String hashType) throws Exception {
        InputStream fis = new FileInputStream(file);
        byte buffer[] = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance(hashType);
        for (int numRead = 0; (numRead = fis.read(buffer)) > 0; ) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        return toHexString(md5.digest());
    }

    private static String toHexString(byte b[]) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            sb.append(Integer.toHexString(aB & 0xFF));
        }
        return sb.toString();
    }

    private String signPackage(String filePath, String keyPasswd) throws IOException, WindowException {
        if (!"".equals(keyPath)) {
            if (filePath != null && !"".equals(filePath)) {
                List<String> rules = new ArrayList<>();
                rules.add("[Ss]ource\\s*:");
                rules.add("[Aa]lgorithm\\s*:");
                rules.add("[Hh]ash\\s*:");
                String in = readMatchLineContent(filePath, rules);
                Signature signature = new Signature();
                Optional<byte[]> signBytes = signature.signMessage(in.trim(), StandardCharsets.UTF_8.toString(), keyPath, keyPasswd);
                if (signBytes.isPresent()) {
                    return new String(signBytes.get(), StandardCharsets.UTF_8);
                } else {
                    throw new WindowException("Failed to sign.");
                }
            } else {
                throw new WindowException("Can not find manifest file.");
            }
        } else {
            throw new WindowException("Can not find private key file.");
        }
    }

//    private static String getFirstFilePath(File file) throws IOException {
//        File[] files = file.listFiles();
//        if (files == null) {
//            return "";
//        }
//        for (File tmp : files) {
//            if (tmp.isFile()) {
//                return tmp.getCanonicalPath();
//            }
//        }
//        return "";
//    }

    private static boolean canLineMatch(String line, List<String> rules) {
        String normalizeLine = Normalizer.normalize(line, Normalizer.Form.NFKC);

        for (String rule : rules) {
            if (normalizeLine.matches(rule) || normalizeLine.split(rule).length > 1) {
                return true;
            }
        }
        return false;
    }

    private static String readMatchLineContent(String fileName, List<String> rules) throws WindowException {
        LineIterator lineIterator;
        StringBuilder result = new StringBuilder();
        String filePath = fileName.replace("\\", File.separator).replace("/", File.separator);
        try (InputStream inputStream = FileUtils.openInputStream(FileUtils.getFile(filePath));
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            lineIterator = new LineIterator(bufferedReader);
            String line;
            LineIterator lineIterator1 = FileUtils.lineIterator(new File(filePath), "UTF-8");

            while (lineIterator.hasNext()) {
                line = lineIterator.next();
                if (line == null || "".equals(line) || canLineMatch(line, rules)) {
                    line += "\n";
                    result.append(line);
                }
            }
        } catch (IOException e) {
            throw new WindowException("");
        }
        return result.toString().trim();
    }
}