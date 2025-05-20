package model;

public class SafeMessage {
    public String TOTP;
    public String IV;
    public String CipherText;

    public SafeMessage(String totp, String ivBase64, String cipherBase64) {
        this.TOTP = totp;
        this.IV = ivBase64;
        this.CipherText = cipherBase64;
    }
}