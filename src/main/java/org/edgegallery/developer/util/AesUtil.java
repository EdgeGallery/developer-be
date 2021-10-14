package org.edgegallery.developer.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AesUtil.class);

    /**
     * AES encryption.
     */
    public static String encode(String clientId, String data) {
        String thisKey = generateKey(clientId);
        if (thisKey == null) {
            return null;
        }
        try {
            //转换key
            Key key = new SecretKeySpec(new BASE64Decoder().decodeBuffer(thisKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new BASE64Encoder().encode(result);
        } catch (Exception e) {
            LOGGER.error("AES encryption fail:{}", e.getMessage());
        }
        return null;
    }

    /**
     * AES decryption.
     */
    public static String decode(String clientId, String data) {
        String thisKey = generateKey(clientId);
        if (thisKey == null) {
            return null;
        }
        try {
            Key key = new SecretKeySpec(new BASE64Decoder().decodeBuffer(thisKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(new BASE64Decoder().decodeBuffer(data));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("AES encryption decode fail:{}", e.getMessage());
        }
        return null;
    }

    /**
     * Generate key.
     */
    public static String generateKey(String clientId) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(clientId.getBytes(StandardCharsets.UTF_8));
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] byteKey = secretKey.getEncoded();
            return new BASE64Encoder().encode(byteKey);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Generate keyfail:{}", e.getMessage());
        }
        return null;
    }

}
