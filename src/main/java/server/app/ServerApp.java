package server.app;

import com.lambdaworks.crypto.SCrypt;
import de.taimos.totp.TOTP;
import model.SafeMessage;
import org.apache.commons.codec.binary.Base32;
import server.auth.AuthenticationService;
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
     * @return TOTP secret para ser armazenado no cliente.
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
     * Obtém um usuário cadastrado no servidor pelo nome.
     * @param name - Nome do usuário.
     * @return Usuário cadastrado ou nulo.
     */
    public static User GetUserByName(String name) {
        Optional<User> opt = UsersRepository.SelectByName(name);
        if (opt.isEmpty())
        {
            System.out.println("Usuário não encontrado");
            return null;
        }
        return opt.get();
    }

    /**
     * Verifica se o usuário passou na autenticação de 3 fatores.
     *
     * @param user Usuário já recuperado do repositório.
     * @param typedPassword Senha informada pelo usuário.
     * @return true se os três fatores forem validados com sucesso, false caso contrário.
     */
    public static Boolean IsUserAuthenticated(User user, String typedPassword) throws Exception {
        return AuthenticationService.Authenticate(user, typedPassword);
    }

    /**
     * Recebe e decifra uma mensagem segura enviada por um cliente autenticado.
     * O processo utiliza AES-GCM com uma chave derivada de TOTP + senha do usuário.
     *
     * @param user Usuário.
     * @param safeMessage  Objeto {@link SafeMessage} contendo a mensagem cifrada, IV e TOTP usado.
     * @throws Exception Se ocorrer erro ao localizar o usuário, derivar a chave ou decifrar a mensagem.
     */
    public static void ReceiveMessage(User user, SafeMessage safeMessage) throws Exception {
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