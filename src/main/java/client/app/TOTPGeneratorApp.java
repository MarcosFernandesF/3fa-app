package client.app;

import client.repository.UserSecret;
import client.repository.UsersSecretsRepository;
import de.taimos.totp.TOTP;
import server.app.ServerApp;
import utils.CryptoUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

public class TOTPGeneratorApp {
    public static void main(String[] args) throws Exception {
        CryptoUtils.InitializeFileEncryption();

        Scanner scanner = new Scanner(System.in);
        UserSecret user = null;
        String name;

        while (true) {
            System.out.print("Nome do usuário (ou digite 'sair' para terminar): ");
            name = scanner.nextLine();

            if ("sair".equalsIgnoreCase(name)) {
                System.out.println("Encerrando o gerador TOTP.");
                return;
            }

            Optional<UserSecret> optUser = UsersSecretsRepository.SelectByName(name); //
            if (optUser.isPresent()) {
                user = optUser.get();
                break;
            } else {
                System.out.println("Usuário '" + name + "' não encontrado no users-secrets.txt. Tente novamente."); //
            }
        }
        String secretHex = CryptoUtils.Base32ToHex(user.TOTPSecret);

        System.out.println("Gerador de TOTP para: " + name);
        String lastCode = "";
        while (true) {
            String code = TOTP.getOTP(secretHex);
            if (!code.equals(lastCode)) {
                System.out.println("TOTP atual: " + code);
                lastCode = code;
            }
            Thread.sleep(1000);
        }
    }
}
