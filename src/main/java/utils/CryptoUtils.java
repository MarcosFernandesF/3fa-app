package utils;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtils {
    public static SecretKey GenerateKey(String passwordHashBase64, byte[] salt, String totp) throws Exception {
        byte[] passwordHash = Base64.getDecoder().decode(passwordHashBase64);
        byte[] passwordPlusTOTP = new byte[passwordHash.length + totp.length()];
        System.arraycopy(passwordHash, 0, passwordPlusTOTP, 0, passwordHash.length);
        System.arraycopy(totp.getBytes(), 0, passwordPlusTOTP, passwordHash.length, totp.length());
        byte[] finalKey = MessageDigest.getInstance("SHA-256").digest(passwordPlusTOTP);
        return new SecretKeySpec(finalKey, 0, 16, "AES");
    }

    public static String base32ToHex(String base32) {
        Base32 codec = new Base32();
        byte[] decoded = codec.decode(base32);
        return Hex.encodeHexString(decoded);
    }
}