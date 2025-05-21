package client.auth;

import server.repository.User;

import java.util.Optional;
import java.util.Scanner;
import server.app.ServerApp;
import server.repository.UsersRepository;

/**
 * Serviço responsável por realizar o processo completo de login 3FA:
 * Senha, Localização (IP) e Verificação do código TOTP.
 */
public class LoginService {

  /**
   * Inicia o processo de login com autenticação 3FA.
   * @return O nome do usuário autenticado com sucesso ou null em caso de falha.
   * @throws Exception Em caso de erro durante os processos de autenticação.
   */
  public static String Start() throws Exception {
    Scanner sc = new Scanner(System.in);

    System.out.println("=== Login de Usuário ===");

    System.out.print("Nome: ");
    String typedName = sc.nextLine();
    System.out.print("Senha: ");
    String typedPassword = sc.nextLine();

    Optional<User> opt = UsersRepository.SelectByName(typedName);
    if (opt.isEmpty())
    {
      System.out.println("Usuário não encontrado");
    }
    User user = opt.get();

    boolean isPasswordCorrect = ServerApp.IsPasswordCorrect(user, typedPassword);
    if (!isPasswordCorrect) return null;
    System.out.println("Senha correta - 1º Fator concluído.");

    boolean isLocationCorrect = ServerApp.IsLocationCorrect(user);
    if (!isLocationCorrect) return null;
    System.out.println("Localização correta - 2º Fator concluído.");

    System.out.print("Código TOTP: ");
    String totp = sc.nextLine();
    boolean isTOTPCorrect = ServerApp.IsTOTPCorrect(user, totp);
    if (!isTOTPCorrect) return null;
    System.out.println("TOTP Correto - 3º Fator concluído.");

    return user.Name;
  }
}