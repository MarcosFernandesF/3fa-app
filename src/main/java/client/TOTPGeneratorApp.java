package client;

import de.taimos.totp.TOTP;
import utils.CryptoUtils;

import java.util.Optional;
import java.util.Scanner;

public class TOTPGeneratorApp {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o nome do usuário: ");
        String name = scanner.nextLine();

        Optional<UserAuthenticator> optUser = UserAuthenticatorRepository.selectUserSecretByName(name);
        if (optUser.isEmpty()) {
            System.out.println("Usuário não encontrado no users-secrets.json.");
            return;
        }

        UserAuthenticator user = optUser.get();
        String secretHex = CryptoUtils.base32ToHex(user.TOTPSecret);

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
