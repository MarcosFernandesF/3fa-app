package client.app;

import client.auth.LoginService;
import client.auth.SignUpService;
import client.message.MessageService;
import client.repository.UserSecret;
import server.repository.User;

public class ClientApp {

    public static void main(String[] args) throws Exception {
        Boolean wannaSignUp = SignUpService.Prompt();

        if (wannaSignUp) SignUpService.Start();

        UserSecret userSecret = LoginService.Start();

        if (userSecret == null) {
            System.out.println("Erro ao fazer login!");
            return;
        }

        MessageService.Start(userSecret);
    }
}