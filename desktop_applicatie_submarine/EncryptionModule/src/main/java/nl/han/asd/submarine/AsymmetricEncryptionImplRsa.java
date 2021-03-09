package nl.han.asd.submarine;

import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.exceptions.UnhandledEncryptionError;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AsymmetricEncryptionImplRsa implements AsymmetricEncryption {
    private static final String CIPHER_NAME = "RSA";
    private static final String CIPHER_WITH_PADDING = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";

    @Override
    public byte[] encrypt(String contentToEncrypt, String publicKey) {
        if (publicKey == null) throw new IllegalArgumentException("The publicKey used for encryption can't be null.");
        if (contentToEncrypt == null)
            throw new IllegalArgumentException("The contentToEncrypt can't be null.");
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_WITH_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, convertStringToPublicKey(publicKey));
            return cipher.doFinal(contentToEncrypt.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to encrypt data: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(byte[] contentToDecrypt, String privateKey) throws IncorrectDecryptionKeyException {
        if (privateKey == null) throw new IllegalArgumentException("The privateKey used for decryption can't be null.");
        if (contentToDecrypt == null)
            throw new IllegalArgumentException("The contentToDecrypt to be decrypted can't be null.");
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_WITH_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, convertStringToPrivateKey(privateKey));
            return new String(cipher.doFinal(contentToDecrypt));
        } catch (BadPaddingException e) {
            throw new IncorrectDecryptionKeyException("This private key is incorrect, the content cannot be decrypted using this key.");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to decrypt data: " + e.getMessage(), e);
        }
    }

    @Override
    public KeyPair generateRandomKeyPair() {
        final int RECOMMENDED_KEY_SIZE = 2048;
        /* There is a range of possible key sizes. Higher key sizes increase safety against brute force decryption attempts, at the cost of exponentially higher decryption times.
        For higher security applications, RSA recommends 2048.
        See https://www.javamex.com/tutorials/cryptography/rsa_key_length.shtml for more information.
        */

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(CIPHER_NAME);
            kpg.initialize(RECOMMENDED_KEY_SIZE);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to generate asymmetric keypair: " + e.getMessage(), e);
        }
    }

    PublicKey convertStringToPublicKey(String publicKey) {
        try {
            byte[] publicBytes = Base64.getDecoder().decode(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            return KeyFactory.getInstance(CIPHER_NAME).generatePublic(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to generate public key from string: " + e.getMessage(), e);
        }
    }

    PrivateKey convertStringToPrivateKey(String privateKey) {
        try {
            byte[] privateBytes = Base64.getDecoder().decode(privateKey.getBytes());
            KeyFactory keyFactory = KeyFactory.getInstance(CIPHER_NAME);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to generate private key from string: " + e.getMessage(), e);
        }
    }
}
