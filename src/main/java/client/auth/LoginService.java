package client.auth;

import client.repository.UserSecret;
import client.repository.UsersSecretsRepository;
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
   * @return Segredo do usuário armazenado no cliente em caso de sucesso ou nulo.
   */
  public static UserSecret Start() throws Exception
  {
    Scanner sc = new Scanner(System.in);

    System.out.println("=== Login de Usuário ===");

    System.out.print("Nome: ");
    String typedName = sc.nextLine();
    System.out.print("Senha: ");
    String typedPassword = sc.nextLine();

    boolean isAuthenticated = ServerApp.IsUserAuthenticated(typedName, typedPassword);

    if (!isAuthenticated) return null;

    Optional<UserSecret> optUserSecret = UsersSecretsRepository.SelectByName(typedName);
    if (optUserSecret.isEmpty()) {
      System.out.println("Usuário não encontrado no users-secrets.txt.");
      return null;
    }
    return optUserSecret.get();
  }
}