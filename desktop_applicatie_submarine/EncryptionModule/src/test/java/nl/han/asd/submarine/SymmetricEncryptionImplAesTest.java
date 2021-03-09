package nl.han.asd.submarine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import nl.han.asd.submarine.encryption.DecryptToObjectUtil;
import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.models.routing.Command;
import nl.han.asd.submarine.models.routing.Node;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.Path;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class SymmetricEncryptionImplAesTest {

    private SymmetricEncryptionImplAes sut;
    private SecretKey symmetricKey;

    private final String UNENCRYPTED_TEXT = "abcd";
    private final String ENCRYPTED_TEXT = "TOjKg4DdM+i68zzwqvlEFA==";

    private final String SYMMETRIC_KEY_AS_BASE64_ENCODED = "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q=";

    private final String SERVER_IP = "94.124.143.166";

    @BeforeEach
    private void setUp() {
        sut = new SymmetricEncryptionImplAes();
        var decodedSymmetricKeyString = Base64.getDecoder().decode(SYMMETRIC_KEY_AS_BASE64_ENCODED);
        symmetricKey = new SecretKeySpec(decodedSymmetricKeyString, 0, decodedSymmetricKeyString.length, "AES");
    }

    @Test
    @DisplayName("Check if a object can be encrypted with AES.")
    void encryptWithAESSuccess() {
        //Arrange
        // Act
        String result = sut.encrypt(UNENCRYPTED_TEXT, symmetricKey);

        // Assert
        assertThat(result).isEqualTo(ENCRYPTED_TEXT);
    }


    @Test
    @DisplayName("Check if new generated symmetric keys are different.")
    void checkSymetricKeysAreDifferent() {
        //Arrange


        // Act
        SecretKey result1 = sut.generateRandomSymmetricKey();
        SecretKey result2 = sut.generateRandomSymmetricKey();

        // Assert
        assertThat(result1).isNotSameAs(result2);
    }

    @Test
    @DisplayName("Encrypt a String, decrypt the String. Check the values")
    void completeEncryptionDecryptionFlowOnString() throws IncorrectDecryptionKeyException {
        String encrypted = sut.encrypt(UNENCRYPTED_TEXT, symmetricKey);
        assertThat(encrypted).isEqualTo(ENCRYPTED_TEXT);

        String decrypted = sut.decrypt(encrypted, SYMMETRIC_KEY_AS_BASE64_ENCODED);
        assertThat(decrypted).isEqualTo(UNENCRYPTED_TEXT);
    }

    @Test
    @DisplayName("Encrypt an Object, decrypt the Object. Check the values")
    void completeEncryptionDecryptionFlowOnObject() throws IncorrectDecryptionKeyException {
        var message = new TextMessage("Thijs", "37", "Dit is mijn bericht");
        String encrypted = sut.encrypt(message, symmetricKey);
        var decrypted = DecryptToObjectUtil.toObject(sut.decrypt(encrypted, SYMMETRIC_KEY_AS_BASE64_ENCODED));
        assertThat(decrypted).isInstanceOf(TextMessage.class);
        assertThat(decrypted).isEqualToComparingFieldByField(message);
    }

    @Test
    @DisplayName("Symmetric key generation output is not null")
    void generateRandomKeyOutputIsNotNull() {
        //Act
        SecretKey result = sut.generateRandomSymmetricKey();

        //Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("If no private key is given when decrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfNoSecretKeyIsGiven() {
        //Arrange

        //Act and Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> sut.decrypt(ENCRYPTED_TEXT, null))
                .withMessage("The secretKey used for decryption can't be null.");

    }

    @Test
    @DisplayName("If no secret key is given when encrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfNoPublicKeyIsGiven() {
        //Arrange

        //Act and Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> sut.encrypt(UNENCRYPTED_TEXT, null))
                .withMessage("The secretKey used for encryption can't be null.");
    }

    @Test
    @DisplayName("If no contentToDecrypt is given when decrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfNoContentToDecryptIsGiven() {
        //Arrange

        //Act and Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> sut.decrypt(null, symmetricKey.toString()))
                .withMessage("The contentToDecrypt to be decrypted can't be null.");
    }

    @Test
    @DisplayName("If no contentToEncrypt is given when encrypting, an IllegalArgumentException is thrown.")
    void illegalArgumentExceptionIsThrownIfContentToEncryptIsGiven() {
        //Arrange

        //Act and Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> sut.encrypt(null, symmetricKey))
                .withMessage("The contentToEncrypt can't be null.");
    }

    @Test
    @DisplayName("Encrypted data cannot be decrypted with the wrong symmetric key")
    void tryToEncryptDataWithWrongSymmetricKey() {
        //Arrange
        SecretKey wrongKey = sut.generateRandomSymmetricKey();

        //Act and Assert
        assertThatExceptionOfType(IncorrectDecryptionKeyException.class)
                .isThrownBy(() -> sut.decrypt(ENCRYPTED_TEXT, "9H+5ObOd2IO/KQcGTTnKR5h4F1DrWFjXt0oud+zlL0Q="))
                .withMessage("This Secret key is incorrect, the content cannot be decrypted using this key.");
    }

    @Test
    @DisplayName("Create onion with three dedicated layers")
    void createOnionWithThreeDedicatedLayers() throws IncorrectDecryptionKeyException {
        // Arrange
        Path path = new Path();
        path.addNodeToPath(new Node(new DestinationRelay("ENTRY_NODE", 1), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));
        path.addNodeToPath(new Node(new DestinationRelay("RELAY_NODE", 2), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));
        path.addNodeToPath(new Node(new DestinationRelay("EXIT_NODE", 3), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));

        // Act
        Onion onion = sut.encryptOnion(path, "ALIAS_OF_RECEIVER", "MESSAGE", "fakeKey", false);

        // Assert
        assertThat(onion.getDestination()).isInstanceOf(DestinationRelay.class);
        var destination = (DestinationRelay) onion.getDestination();
        assertThat(destination.getHostname()).isEqualTo("ENTRY_NODE");
        assertThat(destination.getPort()).isEqualTo(1);
        assertThat(onion.getData()).isNotEqualTo("MESSAGE");
        assertThat(onion.getCommand()).isEqualTo(Command.RELAY.toString());

        Onion onionWithoutEntryNode = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onion.getData(), path.getNodes().get(0).getKey()));
        assertThat(onionWithoutEntryNode.getData()).isNotEqualTo("MESSAGE");
        assertThat(onion.getCommand()).isEqualTo(Command.RELAY.toString());

        Onion onionWithoutRelayNode = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onionWithoutEntryNode.getData(), path.getNodes().get(0).getKey()));
        assertThat(onionWithoutRelayNode.getData()).isNotEqualTo("MESSAGE");
        assertThat(onion.getCommand()).isEqualTo(Command.RELAY.toString());

        Onion onionWithoutExitNode = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onionWithoutRelayNode.getData(), path.getNodes().get(0).getKey()));
        assertThat(onionWithoutExitNode.getData()).isEqualTo("MESSAGE");
        assertThat(onion.getCommand()).isEqualTo(Command.RELAY.toString());
    }

    @Test
    @DisplayName("Create onion with many layers")
    void createOnionWithManyLayers() throws IncorrectDecryptionKeyException {
        final int NODE_AMOUNT = 30;
        Path path = new Path();
        for (int i = 0; i < NODE_AMOUNT; i++) {
            path.addNodeToPath(new Node(new DestinationRelay(i + "", 0), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));
        }

        Onion onion = sut.encryptOnion(path, "ALIAS_OF_RECEIVER", "MESSAGE", "fakeKey", false);

        for (int i = 0; i < NODE_AMOUNT; i++) {
            assertThat(onion.getCommand()).isEqualTo(Command.RELAY.toString());
            onion = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onion.getData(), path.getNodes().get(0).getKey()));
        }
        assertThat(onion.getCommand()).isEqualTo(Command.RELAY.toString());
        assertThat(onion.getData()).isEqualTo("MESSAGE");
    }

    @Test
    @DisplayName("Create onion requires more than one node to function, otherwise an IllegalArgumentException is thrown")
    void createOnionRequiresMoreThanOneNode() {
        Path path = new Path();
        path.addNodeToPath(new Node(new DestinationRelay("lone node", 0), "/E3kzSjyiOp3gS7OU5qW8dRvK3jliQyhcMUj/pzQeLM="));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.encryptOnion(path, "ALIAS_OF_RECEIVER", "MESSAGE", "fakeKey", false))
                .withMessage("An onion requires at least 2 nodes to construct.");
    }

    private void printOnionInJson(Onion onion) {
        // this function is for testing purposes only. With this function you can convert a valid onion to test against the
        // the real Node network.
        var objectMapper = new ObjectMapper();
        try {
            System.out.println(objectMapper.writeValueAsString(onion).length() + new Gson().toJsonTree(onion).toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Create an Onion with a unencrypted json object as data. The command will be HTTP_RELAY except at the last, there it will be HTTP_REQUEST")
    void CanCreateOnionForHTTPRequestWithCommandWithoutAliasOrDecryption() throws IncorrectDecryptionKeyException {
        Path path = new Path();
        path.addNodeToPath(new Node(new DestinationRelay("127.0.0.1", 25010), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));
        path.addNodeToPath(new Node(new DestinationRelay("127.0.0.1", 25011), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));
        path.addNodeToPath(new Node(new DestinationRelay("127.0.0.1", 25012), "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q="));

        String jsonString = "{\"endpoint\":\"http://" + SERVER_IP + "/chatter/login\",\"request_type\":\"PUT\",\"body\":{\"password\":\"htmlIsProgramming\",\"ipAddress\":\"123.123.123\",\"username\":\"HackerMan\"}}\n";

        Onion onion = sut.encryptOnion(path, null, jsonString, symmetricKey.toString(), true);
        assertThat(onion.getCommand()).isEqualTo(Command.HTTP_RELAY.toString());

        Onion onionWithoutEntryNode = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onion.getData(), path.getNodes().get(0).getKey()));
        assertThat(onionWithoutEntryNode.getCommand()).isEqualTo(Command.HTTP_RELAY.toString());

        Onion onionWithoutRelayNode = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onionWithoutEntryNode.getData(), path.getNodes().get(0).getKey()));
        assertThat(onionWithoutRelayNode.getCommand()).isEqualTo(Command.HTTP_RELAY.toString());

        Onion onionWithoutExitNode = (Onion) DecryptToObjectUtil.toObject(sut.decrypt(onionWithoutRelayNode.getData(), path.getNodes().get(0).getKey()));
        assertThat(onionWithoutExitNode.getData()).isEqualTo(jsonString);
        assertThat(onionWithoutExitNode.getCommand()).isEqualTo(Command.HTTP_REQUEST.toString());
    }
}
