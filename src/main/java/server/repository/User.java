package server.repository;

/**
 * Representa um usuário registrado no servidor, incluindo os dados necessários para autenticação 3FA.
 */
public class User {

    /**
     * Nome do usuário.
     */
    public String Name;

    /**
     * País do usuário.
     */
    public String Country;

    /**
     * Hash da senha do usuário.
     */
    public String PasswordHash;

    /**
     * Salt utilizado na derivação do hash da senha.
     */
    public String Salt;

    /**
     * Segredo TOTP em Base32 usado para gerar códigos temporários de autenticação.
     */
    public String TOTPSecret;

    /**
     * Construtor com todos os parâmetros do usuário.
     * @param name         Nome do usuário
     * @param country      País do usuário (ex: \"BR\")
     * @param passwordHash Hash da senha derivado com Scrypt
     * @param salt         Salt usado na derivação do hash
     * @param totpSecret   Segredo TOTP em Base32
     */
    public User(String name, String country, String passwordHash, String salt, String totpSecret) {
        this.Name = name;
        this.Country = country;
        this.PasswordHash = passwordHash;
        this.Salt = salt;
        this.TOTPSecret = totpSecret;
    }
}
