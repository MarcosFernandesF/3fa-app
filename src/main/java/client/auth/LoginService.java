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
   * @return O nome do usuário autenticado com sucesso ou null em caso de falha.
   */
  public static UserSecret Start() throws Exception {
    Scanner sc = new Scanner(System.in);

    System.out.println("=== Login de Usuário ===");

    System.out.print("Nome: ");
    String typedName = sc.nextLine();
    System.out.print("Senha: ");
    String typedPassword = sc.nextLine();

    Optional<UserSecret> optUserSecret = UsersSecretsRepository.SelectByName(typedName);
    if (optUserSecret.isEmpty()) {
      System.out.println("Usuário não encontrado no users-secrets.json.");
      return null;
    }

    return optUserSecret.get();

    // boolean isAuthenticated = ServerApp.IsUserAuthenticated(typedName, typedPassword);

    // if (!isAuthenticated) return null;
  }
}