package utils;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Utilitário de funções criptográficas para o sistema de autenticação 3FA.
 * Responsável por conversões, derivação de chaves e suporte à criptografia simétrica.
 */
public class CryptoUtils {

    /**
     * Gera uma chave simétrica AES de 128 bits a partir de um hash de senha, um salt e um código TOTP.
     * Combina o hash da senha com o TOTP como entrada para um SHA-256, e usa os primeiros 16 bytes como chave AES.
     * @param passwordHashBase64 Hash da senha em Base64 (gerado previamente com Scrypt).
     * @param salt                Salt original usado na derivação do hash da senha.
     * @param totp                Código TOTP atual (Time-based One-Time Password).
     * @return Chave secreta {@link SecretKey} derivada para uso com AES.
     */
    public static SecretKey GenerateKey(String passwordHashBase64, byte[] salt, String totp) throws Exception {
        byte[] passwordHash = Base64.getDecoder().decode(passwordHashBase64);
        byte[] passwordPlusTOTP = new byte[passwordHash.length + totp.length()];

        System.arraycopy(passwordHash, 0, passwordPlusTOTP, 0, passwordHash.length);
        System.arraycopy(totp.getBytes(), 0, passwordPlusTOTP, passwordHash.length, totp.length());

        byte[] finalKey = MessageDigest.getInstance("SHA-256").digest(passwordPlusTOTP);
        return new SecretKeySpec(finalKey, 0, 16, "AES");
    }

    /**
     * Converte uma string codificada em Base32 (ex: segredo TOTP) para uma string hexadecimal.
     * Essa conversão é necessária para o funcionamento do algoritmo TOTP.
     * @param base32 Texto codificado em Base32.
     * @return Representação hexadecimal do valor decodificado.
     */
    public static String Base32ToHex(String base32) {
        Base32 codec = new Base32();
        byte[] decoded = codec.decode(base32);
        return Hex.encodeHexString(decoded);
    }
}
