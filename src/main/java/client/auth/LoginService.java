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
   */
  public static User Start() throws Exception {
    Scanner sc = new Scanner(System.in);

    System.out.println("=== Login de Usuário ===");

    System.out.print("Nome: ");
    String typedName = sc.nextLine();
    System.out.print("Senha: ");
    String typedPassword = sc.nextLine();

    User user = ServerApp.GetUserByName(typedName);
    if (user == null) return null;

    boolean isAuthenticated = ServerApp.IsUserAuthenticated(user, typedPassword);

    if (!isAuthenticated) return null;

    return user;
  }
}