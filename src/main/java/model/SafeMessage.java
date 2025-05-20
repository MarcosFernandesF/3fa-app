package model;

/**
 * Representa uma mensagem cifrada enviada pelo cliente ao servidor após autenticação 3FA.
 * Contém o código TOTP usado na derivação da chave, o vetor de inicialização (IV) e o conteúdo cifrado.
 */
public class SafeMessage {

    /**
     * Código TOTP utilizado no momento da geração da chave simétrica.
     */
    public String TOTP;

    /**
     * Vetor de inicialização (IV) usado na cifragem com AES-GCM, codificado em Base64.
     */
    public String IV;

    /**
     * Conteúdo da mensagem cifrada, codificado em Base64.
     */
    public String CipherText;

    /**
     * Construtor que inicializa todos os campos da mensagem segura.
     * @param totp         Código TOTP utilizado na derivação da chave.
     * @param ivBase64     Vetor de inicialização (IV) em Base64.
     * @param cipherBase64 Mensagem cifrada em Base64.
     */
    public SafeMessage(String totp, String ivBase64, String cipherBase64) {
        this.TOTP = totp;
        this.IV = ivBase64;
        this.CipherText = cipherBase64;
    }
}
