package model;

public class Usuario {
    public String nome;
    public String local;
    public String senhaHashBase64;
    public String saltBase64;
    public String secretTOTP;

    public Usuario() {}
}
