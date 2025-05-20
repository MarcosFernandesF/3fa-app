package server.app;

import model.SafeMessage;
import server.repository.User;
import server.repository.UsersRepository;
import utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;

public class ServerApp {
    public static void receberMensagem(String userName, SafeMessage safeMessage) throws Exception {
        User user = UsersRepository.SelectByName(userName)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no servidor."));

        byte[] salt = Base64.getDecoder().decode(user.Salt);
        SecretKey secretKey = CryptoUtils.GenerateKey(user.PasswordHash, salt, safeMessage.TOTP);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(safeMessage.IV);
        byte[] cipherBytes = Base64.getDecoder().decode(safeMessage.CipherText);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

        String message = new String(cipher.doFinal(cipherBytes));
        System.out.println("📥 Mensagem recebida e decifrada: " + message);
    }
}