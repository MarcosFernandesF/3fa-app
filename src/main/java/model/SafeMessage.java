package model;

/**
 * Representa uma mensagem cifrada enviada pelo cliente ao servidor após autenticação 3FA.
 * Contém o código TOTP usado na derivação da chave, o vetor de inicialização (IV) e o conteúdo cifrado.
 */
public class SafeMessage {

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
     * @param ivBase64     Vetor de inicialização (IV) em Base64.
     * @param cipherBase64 Mensagem cifrada em Base64.
     */
    public SafeMessage(String ivBase64, String cipherBase64) {
        this.IV = ivBase64;
        this.CipherText = cipherBase64;
    }
}
