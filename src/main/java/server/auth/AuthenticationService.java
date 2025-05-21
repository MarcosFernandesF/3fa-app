package server.auth;

import com.lambdaworks.crypto.SCrypt;
import de.taimos.totp.TOTP;
import server.repository.User;
import utils.CryptoUtils;
import utils.IPUtils;

import java.util.Base64;
import java.util.Scanner;

/**
 * Serviço de autenticação 3FA que realiza os três fatores em sequência.
 */
public class AuthenticationService {

  /**
   * Executa o processo de autenticação baseado nos 3 fatores (senha, localização e TOTP).
   *
   * @param user Usuário já recuperado do repositório.
   * @param typedPassword Senha informada pelo usuário.
   * @return true se os três fatores forem validados com sucesso, false caso contrário.
   */
  public static boolean Authenticate(User user, String typedPassword) throws Exception {
    boolean passwordOk = IsPasswordCorrect(user, typedPassword);
    if (!passwordOk) {
      System.out.println("Senha incorreta - falha no 1º fator.");
      return false;
    }
    System.out.println("1 Fator Concluído! Senha correta.");

    boolean locationOk = IsLocationCorrect(user);
    if (!locationOk) {
      System.out.println("Localização incorreta - falha no 2º fator.");
      return false;
    }
    System.out.println("2 Fator Concluído! Localização correta.");

    Scanner scanner = new Scanner(System.in);
    System.out.print("Código TOTP: ");
    String typedTOTP = scanner.nextLine();
    boolean totpOk = IsTOTPCorrect(user, typedTOTP);
    if (!totpOk) {
      System.out.println("TOTP incorreto - falha no 3º fator.");
      return false;
    }
    System.out.println("3 Fator Concluído! TOTP correto.");

    System.out.println("Autenticação 3FA concluída com sucesso!");
    return true;
  }

  /**
   * Valida o primeiro fator: senha.
   * Compara o hash da senha informada com o hash armazenado no usuário.
   * @param user Usuário cujo hash da senha será comparado.
   * @param typedPassword Senha digitada pelo usuário.
   * @return true se o hash da senha informada corresponder ao hash armazenado; false caso contrário.
   */
  public static boolean IsPasswordCorrect(User user, String typedPassword) throws Exception {
    byte[] salt = Base64.getDecoder().decode(user.Salt);
    byte[] typedPasswordHash = SCrypt.scrypt(typedPassword.getBytes(), salt, 16384, 8, 1, 32);
    String typedPasswordHashBase64 = Base64.getEncoder().encodeToString(typedPasswordHash);

    return typedPasswordHashBase64.equals(user.PasswordHash);
  }

  /**
   * Valida o segundo fator: localização geográfica.
   * Compara o país obtido via IP com o país registrado do usuário.
   * @param user Usuário com país de origem previamente registrado.
   * @return true se o país atual for o mesmo do cadastro; false caso contrário.
   */
  public static boolean IsLocationCorrect(User user) throws Exception {
    String currentCountry = IPUtils.GetCurrentCountry();
    return currentCountry.equalsIgnoreCase(user.Country);
  }

  /**
   * Valida o terceiro fator: código TOTP.
   * Compara o TOTP informado com o código esperado baseado no segredo do usuário.
   * @param user Usuário cujo segredo TOTP será usado para gerar o código esperado.
   * @param typedTOTP Código TOTP informado pelo usuário (gerado por um autenticador).
   * @return true se o código TOTP for válido para o momento atual; false caso contrário.
   */
  public static boolean IsTOTPCorrect(User user, String typedTOTP) throws Exception {
    String expectedTotp = TOTP.getOTP(CryptoUtils.Base32ToHex(user.TOTPSecret));
    return expectedTotp.equals(typedTOTP);
  }
}
