package nl.han.asd.submarine.encryption;

import java.security.KeyPair;

public interface AsymmetricEncryption {
    byte[] encrypt(String contentToEncrypt, String publicKey);

    String decrypt(byte[] contentToDecrypt, String privateKey) throws IncorrectDecryptionKeyException;

    KeyPair generateRandomKeyPair();
}
