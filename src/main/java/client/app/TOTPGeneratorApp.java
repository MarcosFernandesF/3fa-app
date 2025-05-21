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
        System.out.print("Nome do usuário: ");
        String name = scanner.nextLine();

        Optional<UserSecret> optUser = UsersSecretsRepository.SelectByName(name);
        if (optUser.isEmpty()) {
            System.out.println("Usuário não encontrado no users-secrets.txt.");
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
