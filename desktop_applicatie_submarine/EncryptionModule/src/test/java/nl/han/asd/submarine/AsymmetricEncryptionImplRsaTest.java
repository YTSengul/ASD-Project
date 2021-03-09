package nl.han.asd.submarine;


import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.exceptions.UnhandledEncryptionError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("WeakerAccess")
//For warnings that mention that unit test classes and methods can be private instead of public even though that would break the unit tests.
class AsymmetricEncryptionImplRsaTest {

    private AsymmetricEncryptionImplRsa sut;

    @BeforeEach
    private void setup() {
        sut = new AsymmetricEncryptionImplRsa();
    }

    @Test
    @DisplayName("Key pair generation is randomized")
    void generateRandomKeypairIsRandom() {
        //Act
        KeyPair kp1 = sut.generateRandomKeyPair();
        KeyPair kp2 = sut.generateRandomKeyPair();
        //Assert
        assertThat(kp1).isNotSameAs(kp2);
        assertThat(kp1.getPrivate().getEncoded()).isNotEqualTo(kp2.getPrivate().getEncoded());
        assertThat(kp1.getPublic().getEncoded()).isNotEqualTo(kp2.getPublic().getEncoded());
    }

    @Test
    @DisplayName("Key pair generation output is not null")
    void generateRandomKeypairOutputIsNotNull() {
        //Act
        KeyPair kp = sut.generateRandomKeyPair();
        //Assert
        assertThat(kp).isNotNull();
        assertThat(kp.getPublic()).isNotNull();
        assertThat(kp.getPrivate()).isNotNull();
        assertThat(kp.getPublic().getEncoded()).isNotNull();
        assertThat(kp.getPrivate().getEncoded()).isNotNull();
    }

    @Test
    @DisplayName("Generated public keys are the proper length")
    void generateRandomKeypairPublicIsLength() {
        int publicKeyEncodedLength = 294;
        //Act
        KeyPair kp = sut.generateRandomKeyPair();
        //Assert
        assertThat(kp.getPublic().getEncoded().length).isEqualTo(publicKeyEncodedLength);
    }

    @Test
    @DisplayName("Generated private keys are the long enough")
    void generateRandomKeypairPrivateIsLength() {
        int privateKeyEncodedLengthMin = 1200;
        //Act
        KeyPair kp = sut.generateRandomKeyPair();
        //Assert
        assertThat(kp.getPrivate().getEncoded().length).isGreaterThan(privateKeyEncodedLengthMin);
    }

    @Test
    @DisplayName("Encryption changes the data at all")
    void encryptionWithPublicKeyWorks() {
        //Arrange
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAktZleUBDdGb+BdxHR2tFpjECOki5JzTiu4fZdz59MrVwTiizB9+Rc8XDW+Qg8AX/AqoDBzzisvz5y9OgUDdW0wTrl72xzdjNq3Ndgev1wD1QubCoJBav49wGw6S/s3ah3YS0CkhoIlbjLBT/hQyC5KNWHA2Enep8sVi1Vbqizedcpxg7J2mC2YO8H+2EOKysOwGz+20gXgu6R5Lioj0Og6qAD5MwQhlIHVtQA3Ks7fd26KLg2Lltq8XWiyoEKVhpDvggIX93hj0X3YNSQLXUM5xoay/xtQGpFT/4aOmE8q4tdjzBXq9LweNqkp4i2jbaXDMvcfyat06piEHS8wFL6wIDAQAB";
        String dataToEncrypt = "This is a test to check if a message can be encrypted";
        //Act
        byte[] encryptedData = sut.encrypt(dataToEncrypt, publicKey);
        //Assert
        assertThat(encryptedData).isNotNull();
        assertThat(encryptedData).isNotEqualTo(dataToEncrypt);
    }


    @Test
    @DisplayName("Encrypted data can be decrypted with matching private key")
    void encryptionFollowedByDecryption() throws IncorrectDecryptionKeyException {
        //Arrange
        KeyPair kp = sut.generateRandomKeyPair();
        String dataToEncrypt = "This is a test to check if a message can be decrypted";
        byte[] encryptedData = sut.encrypt(dataToEncrypt, convertKeyToString(kp.getPublic()));
        //Act
        String decryptedData = sut.decrypt(encryptedData, convertKeyToString(kp.getPrivate()));
        //Assert
        assertThat(decryptedData).isEqualTo(dataToEncrypt);
    }

    @Test
    @DisplayName("Encrypted data cannot be decrypted with a wrong private key")
    void encryptionFollowedByWrongKeyDecryption() {
        //Arrange
        KeyPair kp1 = sut.generateRandomKeyPair();
        KeyPair kp2 = sut.generateRandomKeyPair();
        String dataToEncrypt = "This is a test to check if a message can be decrypted";
        byte[] encryptedData = sut.encrypt(dataToEncrypt, convertKeyToString(kp1.getPublic()));
        //Act and Assert
        assertThatThrownBy(() -> sut.decrypt(encryptedData, convertKeyToString(kp2.getPrivate()))).isInstanceOf(IncorrectDecryptionKeyException.class).hasMessage("This private key is incorrect, the content cannot be decrypted using this key.");
    }

    @Test
    @DisplayName("Encryption is random")
    void encryptionIsRandomized() {
        //Arrange
        String dataToEncrypt = "This is a test to check if a message is encrypted in the right way";
        KeyPair kp = sut.generateRandomKeyPair();
        //Act
        byte[] encryptedData1 = sut.encrypt(dataToEncrypt, convertKeyToString(kp.getPublic()));
        byte[] encryptedData2 = sut.encrypt(dataToEncrypt, convertKeyToString(kp.getPublic()));
        //Assert
        assertThat(encryptedData1).isNotEqualTo(encryptedData2);
    }


    @Test
    @DisplayName("Encryption is random, decryption is not")
    void decryptionHandlesRandomizedEncryption() throws IncorrectDecryptionKeyException {
        //Arrange
        String dataToEncrypt = "Dit is een test om te checken of er een bericht op de juiste manier geencrypt word";
        KeyPair kp = sut.generateRandomKeyPair();
        byte[] encryptedData1 = sut.encrypt(dataToEncrypt, convertKeyToString(kp.getPublic()));
        byte[] encryptedData2 = sut.encrypt(dataToEncrypt, convertKeyToString(kp.getPublic()));
        //Act
        String decryptedData1 = sut.decrypt(encryptedData1, convertKeyToString(kp.getPrivate()));
        String decryptedData2 = sut.decrypt(encryptedData2, convertKeyToString(kp.getPrivate()));
        //Assert
        assertThat(decryptedData1).isEqualTo(decryptedData2);
    }

    @Test
    @DisplayName("If no private key is given when decrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfNoPrivateKeyIsGiven() {
        //Arrange
        KeyPair kp1 = sut.generateRandomKeyPair();
        String dataToEncrypt = "Dit is een test om te checken of er een bericht ge decrypt kan worden";
        byte[] encryptedData = sut.encrypt(dataToEncrypt, convertKeyToString(kp1.getPublic()));
        //Act and Assert
        assertThatThrownBy(() -> sut.decrypt(encryptedData, null)).isInstanceOf(IllegalArgumentException.class).hasMessage("The privateKey used for decryption can't be null.");
    }

    @Test
    @DisplayName("If no public key is given when encrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfNoPublicKeyIsGiven() {
        //Arrange
        String dataToEncrypt = "Dit is een test om te checken of er een bericht gedecrypt kan worden";
        //Act and Assert
        assertThatThrownBy(() -> sut.encrypt(dataToEncrypt, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("The publicKey used for encryption can't be null.");
    }

    @Test
    @DisplayName("If no contentToDecrypt is given when decrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfNoContentToDecryptIsGiven() {
        //Arrange
        KeyPair kp1 = sut.generateRandomKeyPair();
        //Act and Assert
        assertThatThrownBy(() -> sut.decrypt(null, convertKeyToString(kp1.getPrivate()))).isInstanceOf(IllegalArgumentException.class).hasMessage("The contentToDecrypt to be decrypted can't be null.");
    }

    @Test
    @DisplayName("If no contentToEncrypt is given when encrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfContentToEncryptIsGiven() {
        //Arrange
        KeyPair kp1 = sut.generateRandomKeyPair();
        //Act and Assert
        assertThatThrownBy(() -> sut.encrypt(null, convertKeyToString(kp1.getPublic()))).isInstanceOf(IllegalArgumentException.class).hasMessage("The contentToEncrypt can't be null.");
    }

    @Test
    void publicKeyGetsCreatedForString() {
        var publicKey = sut.generateRandomKeyPair().getPublic();

        var result = sut.convertStringToPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));

        assertThat(result).isInstanceOf(PublicKey.class);
        assertThat(result).isEqualTo(publicKey);
    }

    @Test
    void privateKeyGetsCreatedForString() {
        var privateKey = sut.generateRandomKeyPair().getPrivate();

        var result = sut.convertStringToPrivateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()));

        assertThat(result).isInstanceOf(PrivateKey.class);
        assertThat(result).isEqualTo(privateKey);
    }

    @Test
    void exceptionIsThrownForIllegalPublicKeyString() {
        assertThatExceptionOfType(UnhandledEncryptionError.class)
                .isThrownBy(() -> sut.convertStringToPublicKey("123456789123456789"))
                .withMessage("Unexpected error when attempting to generate public key from string: java.security.InvalidKeyException: IOException: null");
    }

    @Test
    void exceptionIsThrownForIllegalPrivateKeyString() {
        assertThatExceptionOfType(UnhandledEncryptionError.class)
                .isThrownBy(() -> sut.convertStringToPrivateKey("123456789123456789"))
                .withMessage("Unexpected error when attempting to generate private key from string: java.security.InvalidKeyException: IOException : null");

    }

    private String convertKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
