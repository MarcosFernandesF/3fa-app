package client.app;

import client.auth.LoginService;
import client.auth.SignUpService;
import client.message.MessageService;
import client.repository.UserSecret;

import java.security.SecureRandom;
import java.util.Base64;

public class ClientApp {

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

        if (SignUpService.Prompt()) {
            SignUpService.Start();
        }

        UserSecret userSecret = LoginService.Start();

        if (userSecret == null) {
            System.out.println("Erro ao fazer login!");
            return;
        }

        MessageService.Start(userSecret);
    }
}