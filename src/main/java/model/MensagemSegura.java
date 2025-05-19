package model;

public class MensagemSegura {
    public String totp;
    public String ivBase64;
    public String cipherBase64;

    public MensagemSegura(String totp, String ivBase64, String cipherBase64) {
        this.totp = totp;
        this.ivBase64 = ivBase64;
        this.cipherBase64 = cipherBase64;
    }
}