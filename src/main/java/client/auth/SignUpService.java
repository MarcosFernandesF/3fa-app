package client.auth;

import client.repository.UserSecret;
import client.repository.UsersSecretsRepository;
import java.util.Scanner;
import server.app.ServerApp;

/**
 * Responsável pelo processo de cadastro de novos usuários no sistema 3FA.
 */
public class SignUpService {

  /**
   * Exibe um prompt perguntando se o usuário deseja se cadastrar e executa o cadastro, se confirmado.
   */
  public static Boolean Prompt() {
    Scanner scanner = new Scanner(System.in);

    System.out.println("=== Deseja cadastrar um usuário? (Sim/Nao) ===");
    String wannaSignUp = scanner.nextLine();
    return wannaSignUp.equalsIgnoreCase("Sim");
  }

  /**
   * Executa o processo completo de cadastro de um novo usuário:
   * - Captura nome e senha
   * - Obtém país via IP
   * - Gera salt, hash da senha e segredo TOTP
   * - Salva o usuário tanto no servidor quanto no cliente (para TOTP)
   */
  public static void Start() {
    Scanner scanner = new Scanner(System.in);

    System.out.println("=== Cadastro de Usuário ===");

    System.out.print("Nome: ");
    String typedName = scanner.nextLine();

    System.out.print("Senha: ");
    String typedPassword = scanner.nextLine();

    try {
      String totpSecret = ServerApp.RegisterUser(typedName, typedPassword);
      UsersSecretsRepository.Add(new UserSecret(typedName, totpSecret));
      System.out.println("Usuário cadastrado com sucesso!");
    } catch (Exception e) {
      System.out.println("Erro ao cadastrar usuário.");
      e.printStackTrace();
    }
  }
}