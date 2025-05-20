package server.app;

import com.lambdaworks.crypto.SCrypt;
import de.taimos.totp.TOTP;
import model.SafeMessage;
import org.apache.commons.codec.binary.Base32;
import server.repository.User;
import server.repository.UsersRepository;
import utils.CryptoUtils;
import utils.IPUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Aplicação do lado do servidor responsável por operações de autenticação 3FA e recebimento de mensagens.
 */
public class ServerApp {

    /**
     * Cadastra um novo usuário no servidor.
     * @param name Nome do usuário
     * @param password Senha em texto claro
     * @return TOTP secret para ser armazenado no cliente
     */
    public static String RegisterUser(String name, String password) throws Exception {
        String country = IPUtils.GetCurrentCountry();
        byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
        byte[] passwordHash = SCrypt.scrypt(password.getBytes(), salt, 16384, 8, 1, 32);
        byte[] secretBytes = new byte[20];
        SecureRandom.getInstanceStrong().nextBytes(secretBytes);
        String totpSecret = new Base32().encodeToString(secretBytes);

        User user = new User(
                name,
                country,
                Base64.getEncoder().encodeToString(passwordHash),
                Base64.getEncoder().encodeToString(salt),
                totpSecret
        );
        UsersRepository.Add(user);

        return totpSecret;
    }

    /**
     * Valida a senha (1º fator).
     */
    public static boolean IsPasswordCorrect(String name, String password) throws Exception {
        Optional<User> opt = UsersRepository.SelectByName(name);
        if (opt.isEmpty()) return false;
        User user = opt.get();

        byte[] salt = Base64.getDecoder().decode(user.Salt);
        byte[] enteredHash = SCrypt.scrypt(password.getBytes(), salt, 16384, 8, 1, 32);
        String enteredHashBase64 = Base64.getEncoder().encodeToString(enteredHash);

        return enteredHashBase64.equals(user.PasswordHash);
    }

    /**
     * Valida a localização por IP (2º fator).
     */
    public static boolean IsLocationCorrect(String name) {
        Optional<User> opt = UsersRepository.SelectByName(name);
        if (opt.isEmpty()) return false;
        User user = opt.get();

        String currentCountry = IPUtils.GetCurrentCountry();
        return currentCountry.equalsIgnoreCase(user.Country);
    }

    /**
     * Valida o código TOTP (3º fator).
     */
    public static boolean IsTOTPCorrect(String name, String totp) {
        Optional<User> opt = UsersRepository.SelectByName(name);
        if (opt.isEmpty()) return false;
        User user = opt.get();

        String expectedTotp = TOTP.getOTP(CryptoUtils.Base32ToHex(user.TOTPSecret));
        return expectedTotp.equals(totp);
    }

    /**
     * Recebe e decifra uma mensagem segura enviada por um cliente autenticado.
     * O processo utiliza AES-GCM com uma chave derivada de TOTP + senha do usuário.
     *
     * @param userName     Nome do usuário remetente da mensagem.
     * @param safeMessage  Objeto {@link SafeMessage} contendo a mensagem cifrada, IV e TOTP usado.
     * @throws Exception Se ocorrer erro ao localizar o usuário, derivar a chave ou decifrar a mensagem.
     */
    public static void ReceiveMessage(String userName, SafeMessage safeMessage) throws Exception {
        // Recupera usuário no lado do servidor
        User user = UsersRepository.SelectByName(userName)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no servidor."));

        // Deriva a chave secreta usando o hash da senha, o salt e o TOTP enviado
        byte[] salt = Base64.getDecoder().decode(user.Salt);
        SecretKey secretKey = CryptoUtils.GenerateKey(user.PasswordHash, salt, safeMessage.TOTP);

        // Inicializa o modo de decifragem AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(safeMessage.IV);
        byte[] cipherBytes = Base64.getDecoder().decode(safeMessage.CipherText);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        // Realiza a decifragem e exibe a mensagem
        String message = new String(cipher.doFinal(cipherBytes));
        System.out.println("Mensagem recebida e decifrada: " + message);
    }
}