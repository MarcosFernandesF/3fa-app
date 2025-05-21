package client.app;

import client.repository.UserSecret;
import client.repository.UsersSecretsRepository;
import de.taimos.totp.TOTP;
import server.app.ServerApp;
import utils.CryptoUtils;

import java.util.Optional;
import java.util.Scanner;

public class TOTPGeneratorApp {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Nome do usuário: ");
        String name = scanner.nextLine();
        System.out.print("Senha: ");
        String typedPassword = scanner.nextLine();

        boolean isPasswordCorrect = ServerApp.IsPasswordCorrect(typedName, typedPassword);

        if (!isPasswordCorrect) {
            System.out.println("Credenciais incorretas");
            return;
        }

        Optional<UserSecret> optUser = UsersSecretsRepository.SelectByName(name);
        if (optUser.isEmpty()) {
            System.out.println("Usuário não encontrado no users-secrets.json.");
            return;
        }
        UserSecret user = optUser.get();
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
