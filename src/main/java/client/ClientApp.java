package client;

import com.lambdaworks.crypto.SCrypt;
import de.taimos.totp.TOTP;
import model.SafeMessage;
import model.User;
import server.ServerApp;
import utils.CryptoUtils;
import utils.IPUtils;
import server.UserRepository;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base32;

public class ClientApp {

    public static void Start() throws Exception {
        SignUp();

        String nome = Login();

        if (nome == null) return;

        Message(nome);
    }

    public static void SignUp() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Deseja cadastrar um usuário? (Sim/Nao) ===");
        String wannaSignUp = scanner.nextLine();
        if (!wannaSignUp.equalsIgnoreCase("Sim")) return;

        System.out.println("=== Cadastro de Usuário ===");

        System.out.print("Nome: ");
        String name = scanner.nextLine();

        System.out.print("Senha: ");
        String password = scanner.nextLine();

        String country = IPUtils.GetCurrentCountry();
        System.out.println("Detectado país: " + country);

        try {
            byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
            byte[] passwordHash = SCrypt.scrypt(password.getBytes(), salt, 16384, 8, 1, 32);

            byte[] secretBytes = new byte[20];
            SecureRandom.getInstanceStrong().nextBytes(secretBytes);
            String totpSecret = new Base32().encodeToString(secretBytes);

            User user = new User();
            user.Name = name;
            user.Country = country;
            user.Salt = Base64.getEncoder().encodeToString(salt);
            user.PasswordHash = Base64.getEncoder().encodeToString(passwordHash);
            user.TOTPSecret = totpSecret;
            UserRepository.AddUser(user);

            UserAuthenticator userAuthenticator = new UserAuthenticator();
            userAuthenticator.Name = name;
            userAuthenticator.TOTPSecret = totpSecret;
            UserAuthenticatorRepository.addUserAuthenticator(userAuthenticator);

            System.out.println("Usuário cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar usuário.");
            System.out.println(e);
        }
    }

    public static String Login() throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Login 3FA ===");
        System.out.print("Digite seu nome de usuário: ");
        String name = scanner.nextLine();

        Optional<User> optUser = UserRepository.SelectUserByName(name);
        if (optUser.isEmpty()) {
            System.out.println("Usuário não encontrado.");
            return null;
        }

        User user = optUser.get();

        // 1. Verificação da senha (SCrypt)
        System.out.print("Digite sua senha: ");
        String passwordEntered = scanner.nextLine();
        byte[] salt = Base64.getDecoder().decode(user.Salt);
        byte[] passwordEnteredHash = SCrypt.scrypt(passwordEntered.getBytes(), salt, 16384, 8, 1, 32);
        String passwordEnteredHashBase64 = Base64.getEncoder().encodeToString(passwordEnteredHash);

        if (!passwordEnteredHashBase64.equals(user.PasswordHash)) {
            System.out.println("Senha incorreta.");
            return null;
        }
        System.out.println("Senha verificada com sucesso!");

        // 2. Verificação da localização
        String currentCountry = IPUtils.GetCurrentCountry();
        if (!currentCountry.equalsIgnoreCase(user.Country)) {
            System.out.println("Localização inválida! Seu IP atual indica: " + currentCountry);
            return null;
        }
        System.out.println("Localização verificada: " + currentCountry + "!");

        // 3. Verificação do TOTP
        System.out.print("Digite o código TOTP do seu aplicativo autenticador: ");
        String codeEntered = scanner.nextLine();
        String generatedCode = TOTP.getOTP(CryptoUtils.base32ToHex(user.TOTPSecret));

        if (!codeEntered.equals(generatedCode)) {
            System.out.println("TOTP incorreto. Acesso negado.");
            return null;
        }

        System.out.println("TOTP válido. Login realizado com sucesso!");

        return name;
    }

    public static void Message(String userName) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while(true)
        {
            System.out.println("== Enviar Mensagem Segura ==");

            System.out.print("Digite a mensagem a ser enviada: ");
            String plainText = scanner.nextLine();

            SafeMessage cipherText = ClientApp.SendMessage(userName, plainText);
            ServerApp.receberMensagem(userName, cipherText);

            System.out.print("Deseja enviar outra mensagem?");
            String response = scanner.nextLine().trim();
            if (!response.equalsIgnoreCase("Sim"))
            {
                System.out.println("Encerrando sessão.");
                break;
            }
        }
    }

    public static SafeMessage SendMessage(String userName, String plainText) throws Exception {
        User user = UserRepository.SelectUserByName(userName)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String currentCountry = IPUtils.GetCurrentCountry();
        if (!currentCountry.equalsIgnoreCase(user.Country)) {
            throw new SecurityException("Localização inválida! Acesso negado.");
        }

        String totp = TOTP.getOTP(CryptoUtils.base32ToHex(user.TOTPSecret));
        byte[] salt = Base64.getDecoder().decode(user.Salt);
        SecretKey secretKey = CryptoUtils.GenerateKey(user.PasswordHash, salt, totp);

        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());

        return new SafeMessage(
                totp,
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherText)
        );
    }
}