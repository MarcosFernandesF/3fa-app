package client;

import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.util.Optional;

public class TOTPGeneratorApp {

    public static String base32ToHex(String base32) {
        Base32 codec = new Base32();
        byte[] decoded = codec.decode(base32);
        return Hex.encodeHexString(decoded);
    }

    public static void main(String[] args) throws Exception {
        String nome = "nome1"; // ou peça via Scanner se quiser tornar dinâmico

        Optional<UserAuthenticator> optUser = UserAuthenticatorStore.findByNome(nome);
        if (optUser.isEmpty()) {
            System.out.println("Usuário não encontrado no secrets.json.");
            return;
        }

        UserAuthenticator user = optUser.get();
        String secretHex = base32ToHex(user.secretTOTP);

        System.out.println("Gerador de TOTP para: " + nome);
        String lastCode = "";
        while (true) {
            String code = TOTP.getOTP(secretHex);
            if (!code.equals(lastCode)) {
                System.out.println("TOTP atual: " + code);
                lastCode = code;
            }
            Thread.sleep(1000);
        }
    }
}
