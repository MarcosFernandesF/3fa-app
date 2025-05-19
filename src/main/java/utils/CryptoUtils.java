package utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtils {
    public static SecretKey gerarChave(String senhaHashBase64, byte[] salt, String totp) throws Exception {
        byte[] senhaHash = Base64.getDecoder().decode(senhaHashBase64);
        byte[] combinado = new byte[senhaHash.length + totp.length()];
        System.arraycopy(senhaHash, 0, combinado, 0, senhaHash.length);
        System.arraycopy(totp.getBytes(), 0, combinado, senhaHash.length, totp.length());
        byte[] chaveFinal = MessageDigest.getInstance("SHA-256").digest(combinado);
        return new SecretKeySpec(chaveFinal, 0, 16, "AES");
    }
}