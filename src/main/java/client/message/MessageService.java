package client.message;

import model.SafeMessage;
import server.app.ServerApp;
import server.repository.User;
import server.repository.UsersRepository;
import utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

import de.taimos.totp.TOTP;

/**
 * Serviço responsável por envio de mensagens seguras entre cliente e servidor.
 */
public class MessageService {

  /**
   * Inicia o processo de envio de mensagens.
   * @param userName Nome do usuário.
   */
  public static void Start(String userName) throws Exception {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.println("== Enviar Mensagem Segura ==");
      System.out.print("Digite a mensagem a ser enviada: ");
      String plainText = scanner.nextLine();

      SafeMessage cipherText = GetSafeMessage(userName, plainText);
      ServerApp.ReceiveMessage(userName, cipherText);

      System.out.print("Deseja enviar outra mensagem? (Sim/Nao): ");
      String response = scanner.nextLine().trim();
      if (!response.equalsIgnoreCase("Sim")) {
        System.out.println("Encerrando sessão.");
        break;
      }
    }
  }

  /**
   * Obtém uma mensagem segura utilizando AES-GCM e chave derivada do TOTP.
   * @param userName  Nome do usuário remetente
   * @param plainText Mensagem em texto claro a ser cifrada
   * @return Objeto {@link SafeMessage} contendo TOTP, IV e texto cifrado
   */
  public static SafeMessage GetSafeMessage(String userName, String plainText) throws Exception {
    User user = UsersRepository.SelectByName(userName)
      .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

    String totp = TOTP.getOTP(CryptoUtils.Base32ToHex(user.TOTPSecret));
    byte[] salt = Base64.getDecoder().decode(user.Salt);
    SecretKey secretKey = CryptoUtils.GenerateKey(user.PasswordHash, salt, totp);

    byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    GCMParameterSpec spec = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

    byte[] cipherBytes = cipher.doFinal(plainText.getBytes());

    return new SafeMessage(
      totp,
      Base64.getEncoder().encodeToString(iv),
      Base64.getEncoder().encodeToString(cipherBytes)
    );
  }
}
