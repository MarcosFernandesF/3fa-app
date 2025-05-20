package client.repository;

/**
 * Representa o segredo TOTP associado a um usuário no lado do cliente.
 * Utilizado para gerar códigos temporários de autenticação (TOTP).
 */
public class UserSecret {
    /**
     * Nome do usuário.
     */
    public String Name;

    /**
     * Segredo TOTP em Base32 utilizado para gerar os códigos de autenticação.
     */
    public String TOTPSecret;

    /**
     * Construtor padrão necessário para serialização/deserialização JSON.
     */
    public UserSecret() {}

    /**
     * Construtor que define o nome do usuário e o segredo TOTP.
     * @param name        Nome do usuário
     * @param totpSecret  Segredo TOTP em Base32
     */
    public UserSecret(String name, String totpSecret) {
        this.Name = name;
        this.TOTPSecret = totpSecret;
    }
}
