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
        String masterPassword = System.getenv("APP_MASTER_PASSWORD");
        String fileEncryptionSalt = System.getenv("APP_ENCRYPTION_SALT");

        if (masterPassword == null || masterPassword.isEmpty()) {
            System.err.println("ERRO CRÍTICO: Senha mestra não definida na variável de ambiente APP_MASTER_PASSWORD.");
            System.err.println("A aplicação não pode operar de forma segura sem ela.");
            System.exit(1); // Termina a aplicação se a senha não estiver configurada
        }

        try {
            utils.CryptoUtils.InitializeFileEncryptionKey(masterPassword, fileEncryptionSalt);
            System.out.println("Mecanismo de criptografia de arquivo inicializado.");
        } catch (Exception e) {
            System.err.println("Falha ao inicializar o mecanismo de criptografia de arquivo: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

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
