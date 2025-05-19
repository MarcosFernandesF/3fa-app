package server;

import model.MensagemSegura;
import model.Usuario;
import utils.CryptoUtils;
import utils.UserStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;

public class ServerApp {
    public static void receberMensagem(String nomeUsuario, MensagemSegura msg) throws Exception {
        Usuario user = UserStore.findByNome(nomeUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no servidor."));

        byte[] salt = Base64.getDecoder().decode(user.saltBase64);
        SecretKey chave = CryptoUtils.gerarChave(user.senhaHashBase64, salt, msg.totp);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(msg.ivBase64);
        byte[] cipherBytes = Base64.getDecoder().decode(msg.cipherBase64);
        cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(128, iv));

        String mensagem = new String(cipher.doFinal(cipherBytes));
        System.out.println("📥 Mensagem recebida e decifrada: " + mensagem);
    }
}