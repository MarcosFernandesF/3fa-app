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
import java.util.Scanner;

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
    public static String RegisterUser(String name, String password) throws Exception
    {
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
    public static User GetUserByName(String name)
    {
        Optional<User> opt = UsersRepository.SelectByName(name);
        if (opt.isEmpty())
        {
            System.out.println("Usuário não encontrado");
            return null;
        }
        return opt.get();
    }

    /**
     * Executa o processo de autenticação baseado nos 3 fatores (senha, localização e TOTP).
     * @param typedName Nome informado pelo usuário.
     * @param typedPassword Senha informada pelo usuário.
     * @return true se os três fatores forem validados com sucesso, false caso contrário.
     */
    public static boolean IsUserAuthenticated(String typedName, String typedPassword) throws Exception
    {
        User user = ServerApp.GetUserByName(typedName);
        if (user == null) return false;

        boolean passwordOk = IsPasswordCorrect(user, typedPassword);
        if (!passwordOk) {
            System.out.println("Senha incorreta - falha no 1º fator.");
            return false;
        }
        System.out.println("1 Fator Concluído! Senha correta.");

        boolean locationOk = IsLocationCorrect(user);
        if (!locationOk) {
            System.out.println("Localização incorreta - falha no 2º fator.");
            return false;
        }
        System.out.println("2 Fator Concluído! Localização correta.");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Código TOTP: ");
        String typedTOTP = scanner.nextLine();
        boolean totpOk = IsTOTPCorrect(user, typedTOTP);
        if (!totpOk) {
            System.out.println("TOTP incorreto - falha no 3º fator.");
            return false;
        }
        System.out.println("3 Fator Concluído! TOTP correto.");

        System.out.println("Autenticação 3FA concluída com sucesso!");
        return true;
    }

    /**
     * Valida o primeiro fator: senha.
     * Compara o hash da senha informada com o hash armazenado no usuário.
     * @param user Usuário cujo hash da senha será comparado.
     * @param typedPassword Senha digitada pelo usuário.
     * @return true se o hash da senha informada corresponder ao hash armazenado; false caso contrário.
     */
    public static boolean IsPasswordCorrect(User user, String typedPassword) throws Exception
    {
        byte[] salt = Base64.getDecoder().decode(user.Salt);
        byte[] typedPasswordHash = SCrypt.scrypt(typedPassword.getBytes(), salt, 16384, 8, 1, 32);
        String typedPasswordHashBase64 = Base64.getEncoder().encodeToString(typedPasswordHash);

        return typedPasswordHashBase64.equals(user.PasswordHash);
    }

    /**
     * Valida o segundo fator: localização geográfica.
     * Compara o país obtido via IP com o país registrado do usuário.
     * @param user Usuário com país de origem previamente registrado.
     * @return true se o país atual for o mesmo do cadastro; false caso contrário.
     */
    public static boolean IsLocationCorrect(User user) throws Exception
    {
        String currentCountry = IPUtils.GetCurrentCountry();
        return currentCountry.equalsIgnoreCase(user.Country);
    }

    /**
     * Valida o terceiro fator: código TOTP.
     * Compara o TOTP informado com o código esperado baseado no segredo do usuário.
     * @param user Usuário cujo segredo TOTP será usado para gerar o código esperado.
     * @param typedTOTP Código TOTP informado pelo usuário (gerado por um autenticador).
     * @return true se o código TOTP for válido para o momento atual; false caso contrário.
     */
    public static boolean IsTOTPCorrect(User user, String typedTOTP) throws Exception
    {
        String expectedTotp = TOTP.getOTP(CryptoUtils.Base32ToHex(user.TOTPSecret));
        return expectedTotp.equals(typedTOTP);
    }

    /**
     * Recebe e decifra uma mensagem segura enviada por um cliente autenticado.
     * O processo utiliza AES-GCM com uma chave derivada de TOTP + senha do usuário.
     *
     * @param userName Nome do usuário.
     * @param safeMessage  Objeto {@link SafeMessage} contendo a mensagem cifrada, IV e TOTP usado.
     * @throws Exception Se ocorrer erro ao localizar o usuário, derivar a chave ou decifrar a mensagem.
     */
    public static void ReceiveMessage(String userName, SafeMessage safeMessage) throws Exception
    {
        User user = GetUserByName(userName);

        if (user == null)
        {
            System.out.println("Usuário não encontrado do lado do servidor");
            return;
        }

        SecretKey secretKey = CryptoUtils.GenerateKeyFromTOTP(user.TOTPSecret);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(safeMessage.IV);
        byte[] cipherBytes = Base64.getDecoder().decode(safeMessage.CipherText);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        String message = new String(cipher.doFinal(cipherBytes));
        System.out.println("Mensagem recebida e decifrada: " + message);
    }
}