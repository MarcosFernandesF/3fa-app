package client.app;

import client.auth.LoginService;
import client.auth.SignUpService;
import client.message.MessageService;

public class ClientApp {

    public static void main(String[] args) throws Exception {
        Boolean wannaSignUp = SignUpService.Prompt();

        if (wannaSignUp) SignUpService.Start();

        String userName = LoginService.Start();

        if (userName != null) MessageService.Start(userName);
    }
}