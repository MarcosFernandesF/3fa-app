package client.app;

import client.auth.LoginService;
import client.auth.SignUpService;
import client.message.MessageService;
import client.repository.UserSecret;
import utils.CryptoUtils;

import java.security.SecureRandom;
import java.util.Base64;

public class ClientApp {

    public static void main(String[] args) throws Exception {

        CryptoUtils.InitializeFileEncryption();


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