package client;

import com.lambdaworks.crypto.SCrypt;
import de.taimos.totp.TOTP;
import model.MensagemSegura;
import model.Usuario;
import server.ServerApp;
import utils.CryptoUtils;
import utils.IPUtils;
import utils.UserStore;
import client.UserAuthenticatorStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

public class ClientApp {

    public static void Iniciar() throws Exception {
        Cadastro();

        String nome = Login();

        if (nome == null) return;

        Mensagem(nome);
    }

    public static void Cadastro() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Deseja cadastrar um usuário? (Sim/Nao) ===");
        String desejaCadastrar = scanner.nextLine();
        if (!desejaCadastrar.equalsIgnoreCase("Sim")) return;

        System.out.println("=== Cadastro de Usuário ===");

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        String pais = IPUtils.getPaisAtual();
        System.out.println("Detectado país: " + pais);

        try {
            byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
            byte[] senhaHash = SCrypt.scrypt(senha.getBytes(), salt, 16384, 8, 1, 32);

            byte[] secretBytes = new byte[20];
            SecureRandom.getInstanceStrong().nextBytes(secretBytes);
            String secretTOTP = new Base32().encodeToString(secretBytes);

            Usuario user = new Usuario();
            user.nome = nome;
            user.local = pais;
            user.saltBase64 = Base64.getEncoder().encodeToString(salt);
            user.senhaHashBase64 = Base64.getEncoder().encodeToString(senhaHash);
            user.secretTOTP = secretTOTP;

            UserStore.addUsuario(user);

            UserAuthenticator userAuthenticator = new UserAuthenticator();
            userAuthenticator.nome = nome;
            userAuthenticator.secretTOTP = secretTOTP;
            UserAuthenticatorStore.addUserAuthenticator(userAuthenticator);

            System.out.println("Usuário cadastrado com sucesso.");
            System.out.println("Adicione esse segredo no seu app autenticador: " + secretTOTP);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String Login() throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Login 3FA ===");
        System.out.print("Digite seu nome de usuário: ");
        String nome = scanner.nextLine();

        Optional<Usuario> optUser = UserStore.findByNome(nome);
        if (optUser.isEmpty()) {
            System.out.println("Usuário não encontrado.");
            return null;
        }

        Usuario user = optUser.get();

        // 1. Verificação da senha (SCrypt)
        System.out.print("Digite sua senha: ");
        String senhaDigitada = scanner.nextLine();
        byte[] salt = Base64.getDecoder().decode(user.saltBase64);
        byte[] senhaHashDigitada = SCrypt.scrypt(senhaDigitada.getBytes(), salt, 16384, 8, 1, 32);
        String senhaHashDigitadaBase64 = Base64.getEncoder().encodeToString(senhaHashDigitada);

        if (!senhaHashDigitadaBase64.equals(user.senhaHashBase64)) {
            System.out.println("Senha incorreta.");
            return null;
        }
        System.out.println("✔️ Senha verificada com sucesso.");

        // 2. Verificação da localização
        String paisAtual = IPUtils.getPaisAtual();
        if (!paisAtual.equalsIgnoreCase(user.local)) {
            System.out.println("Localização inválida! Seu IP atual indica: " + paisAtual);
            return null;
        }
        System.out.println("✔️ Localização verificada: " + paisAtual);

        // 3. Verificação do TOTP
        System.out.print("Digite o código TOTP do seu aplicativo autenticador: ");
        String codigoDigitado = scanner.nextLine();
        String codigoGerado = TOTP.getOTP(base32ToHex(user.secretTOTP));

        if (!codigoDigitado.equals(codigoGerado)) {
            System.out.println("TOTP incorreto. Acesso negado.");
            return null;
        }

        System.out.println("✔️ TOTP válido. Login realizado com sucesso!");

        return nome;
    }

    public static void Mensagem(String nome) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while(true)
        {
            System.out.println("== Enviar Mensagem Segura ==");

            System.out.print("Digite a mensagem a ser enviada: ");
            String textoPlano = scanner.nextLine();

            MensagemSegura textoCifrado = ClientApp.enviarMensagem(nome, textoPlano);
            ServerApp.receberMensagem(nome, textoCifrado);

            System.out.print("Deseja enviar outra mensagem?");
            String resposta = scanner.nextLine().trim();
            if (!resposta.equalsIgnoreCase("Sim"))
            {
                System.out.println("Encerrando sessão.");
                break;
            }
        }
    }

    public static MensagemSegura enviarMensagem(String nomeUsuario, String textoClaro) throws Exception {
        Usuario user = UserStore.findByNome(nomeUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String paisAtual = IPUtils.getPaisAtual();
        if (!paisAtual.equalsIgnoreCase(user.local)) {
            throw new SecurityException("Localização inválida! Acesso negado.");
        }

        String totp = TOTP.getOTP(base32ToHex(user.secretTOTP));
        byte[] salt = Base64.getDecoder().decode(user.saltBase64);
        SecretKey chave = CryptoUtils.gerarChave(user.senhaHashBase64, salt, totp);

        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, chave, spec);
        byte[] cifrado = cipher.doFinal(textoClaro.getBytes());

        return new MensagemSegura(
                totp,
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cifrado)
        );
    }

    public static String base32ToHex(String base32) {
        Base32 base32Codec = new Base32();
        byte[] decoded = base32Codec.decode(base32);
        return Hex.encodeHexString(decoded);
    }
}