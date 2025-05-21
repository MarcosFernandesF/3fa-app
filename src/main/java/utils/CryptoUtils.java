package utils;

import com.lambdaworks.crypto.SCrypt;
import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário de funções criptográficas para o sistema de autenticação 3FA.
 * Responsável por conversões, derivação de chaves e suporte à criptografia simétrica.
 */
public class CryptoUtils {

    private static SecretKey fileEncryptionKey; // Chave para criptografar os JSONs
    private static final int GCM_IV_LENGTH = 12; // bytes
    private static final int GCM_TAG_LENGTH = 128; // bits

    /**
     * Inicializa a chave de criptografia de arquivos a partir de uma senha mestra.
     * DEVE ser chamado uma vez no início da aplicação.
     */
    public static void InitializeFileEncryption() throws Exception {
        String masterPassword = System.getenv("APP_MASTER_PASSWORD");
        String fileEncryptionSalt = System.getenv("APP_ENCRYPTION_SALT");

        if (masterPassword == null || masterPassword.isEmpty()) {
            System.err.println("ERRO CRÍTICO: Senha mestra não definida na variável de ambiente APP_MASTER_PASSWORD.");
            System.err.println("A aplicação não pode operar de forma segura sem ela.");
            System.exit(1); // Termina a aplicação se a senha não estiver configurada
        }

        try {
            byte[] derivedKey = SCrypt.scrypt(
                    masterPassword.getBytes(StandardCharsets.UTF_8),
                    fileEncryptionSalt.getBytes(StandardCharsets.UTF_8),
                    16384, 8, 1, 32);
            fileEncryptionKey = new SecretKeySpec(derivedKey, "AES");
            System.out.println("Mecanismo de criptografia de arquivo inicializado.");
        } catch (Exception e) {
            System.err.println("Falha ao inicializar o mecanismo de criptografia de arquivo: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Criptografa dados (ex: conteúdo JSON) usando AES/GCM.
     * @param plaintext Os dados em texto claro.
     * @return String Base64 contendo IV + Ciphertext.
     */
    public static String EncryptFileData(String plaintext) throws Exception {
        if (fileEncryptionKey == null) {
            throw new IllegalStateException("Chave de criptografia de arquivo não inicializada.");
        }
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = SecureRandom.getInstanceStrong();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, fileEncryptionKey, gcmParameterSpec);

        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Concatena IV + Ciphertext e codifica em Base64
        byte[] ivAndCipherText = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
        System.arraycopy(cipherText, 0, ivAndCipherText, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(ivAndCipherText);
    }

    /**
     * Descriptografa dados (ex: conteúdo JSON) usando AES/GCM.
     * @param base64IvAndCipherText String Base64 contendo IV + Ciphertext.
     * @return Os dados em texto claro.
     */
    public static String DecryptFileData(String base64IvAndCipherText) throws Exception {
        if (fileEncryptionKey == null) {
            throw new IllegalStateException("Chave de criptografia de arquivo não inicializada.");
        }
        byte[] decodedIvAndCipherText = Base64.getDecoder().decode(base64IvAndCipherText);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedIvAndCipherText, 0, iv, 0, iv.length);

        byte[] cipherText = new byte[decodedIvAndCipherText.length - iv.length];
        System.arraycopy(decodedIvAndCipherText, iv.length, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, fileEncryptionKey, gcmParameterSpec);

        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    /**
     * Gera uma chave simétrica AES de 128 bits a partir do segredo TOTP.
     * @param totpSecret Segredo TOTP em base32.
     * @return Chave secreta {@link SecretKey} derivada para uso com AES.
     */
    public static SecretKey GenerateKeyFromTOTP(String totpSecret) throws Exception {
        String hexKey = Base32ToHex(totpSecret);
        String totp = TOTP.getOTP(hexKey);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(totp.getBytes());
        return new SecretKeySpec(hash, 0, 16, "AES");
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
