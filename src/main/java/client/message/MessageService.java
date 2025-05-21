package client.message;

import client.repository.UserSecret;
import server.app.ServerApp;
import utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

/**
 * Serviço responsável por envio de mensagens seguras entre cliente e servidor.
 */
public class MessageService {

    private static final int GCM_IV_LENGTH_BYTES = 12; // Definindo o tamanho do IV consistentemente

    /**
     * Inicia o processo de envio de mensagens.
     * @param userSecret Secret do usuário armazenado no cliente.
     */
    public static void Start(UserSecret userSecret) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("== Enviar Mensagem Segura ==");
            System.out.print("Digite a mensagem a ser enviada: ");
            String plainText = scanner.nextLine();

            // GetSafeMessage agora retorna uma String (IV+CipherText em Base64)
            String encryptedMessagePayload = GetSafeMessage(userSecret.TOTPSecret, plainText);
            ServerApp.ReceiveMessage(userSecret.Name, encryptedMessagePayload);

            System.out.print("Deseja enviar outra mensagem? (Sim/Nao): ");
            String response = scanner.nextLine().trim();
            if (!response.equalsIgnoreCase("Sim")) {
                System.out.println("Encerrando sessão.");
                break;
            }
        }
    }

    /**
     * Cifra uma mensagem utilizando AES-GCM e chave derivada do TOTP.
     * O IV e o texto cifrado são concatenados e codificados em Base64.
     * @param totpSecret Segredo TOTP do usuário.
     * @param plainText Mensagem em texto claro a ser cifrada.
     * @return String Base64 contendo IV + CipherText.
     */
    public static String GetSafeMessage(String totpSecret, String plainText) throws Exception {
        SecretKey secretKey = CryptoUtils.GenerateKeyFromTOTP(totpSecret);

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        SecureRandom random = SecureRandom.getInstanceStrong();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] ivAndCipherText = new byte[iv.length + cipherBytes.length];
        System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
        System.arraycopy(cipherBytes, 0, ivAndCipherText, iv.length, cipherBytes.length);

        return Base64.getEncoder().encodeToString(ivAndCipherText);
    }
}