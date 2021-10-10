package org.edgegallery.developer.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

//    public static void main(String[] args) throws UnsupportedEncodingException {
//        String key = generateKey("developer-fe");
//        System.out.println(key);
//        String ps = encode(key, "123");
//        String res = decode(key, ps);
//    }

    /**
     * AES加密
     */
    public static String encode(String clientId, String data) {
        String thisKey = generateKey(clientId);
        if (thisKey==null) {
            return null;
        }
        try {
            //转换key
            Key key = new SecretKeySpec((new BASE64Decoder()).decodeBuffer(thisKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(data.getBytes());
            return (new BASE64Encoder()).encode(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     */
    public static String decode(String clientId, String data) {
        String thisKey = generateKey(clientId);
        if (thisKey==null) {
            return null;
        }
        try {
            Key key = new SecretKeySpec((new BASE64Decoder()).decodeBuffer(thisKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal((new BASE64Decoder()).decodeBuffer(data));
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 生成key
     */
    public static String generateKey(String clientId) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(clientId.getBytes());
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(secureRandom);
            SecretKey secretKey =  keyGenerator.generateKey();
            byte[] byteKey = secretKey.getEncoded();
            return (new BASE64Encoder()).encode(byteKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
