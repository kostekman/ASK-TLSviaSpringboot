package agh.edu.koscinsa.ask.server.component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DESUtil {
    public static String encrypt(String message, byte[] secret) throws Exception{
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, sf.generateSecret(new DESKeySpec(secret)));

        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String message, byte[] secret) throws Exception{
        byte[] encodedMessage = Base64.getDecoder().decode(message.getBytes());
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, sf.generateSecret(new DESKeySpec(secret)));

        return new String(cipher.doFinal(encodedMessage));
    }
}
