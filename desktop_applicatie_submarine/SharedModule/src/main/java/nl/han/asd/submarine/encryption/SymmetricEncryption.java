package nl.han.asd.submarine.encryption;

import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.Path;

import javax.crypto.SecretKey;

public interface SymmetricEncryption {

    String encrypt(Object contentToEncrypt, SecretKey secretKey);

    String decrypt(String encrypted, String keyString) throws IncorrectDecryptionKeyException;

    SecretKey generateRandomSymmetricKey();

    Onion encryptOnion(Path path, String receiverAlias, String encryptedMessage, String encryptedSymmetricKey, boolean isHTTPRequest);

    SecretKey convertKeyStringToSecretKeySpec(String keyString);
}
