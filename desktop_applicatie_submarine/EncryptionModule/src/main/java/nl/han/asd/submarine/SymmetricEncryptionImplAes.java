package nl.han.asd.submarine;

import com.google.gson.*;
import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.exceptions.UnhandledEncryptionError;
import nl.han.asd.submarine.models.routing.Command;
import nl.han.asd.submarine.models.routing.Node;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.Path;
import nl.han.asd.submarine.models.routing.destination.Destination;
import nl.han.asd.submarine.models.routing.destination.DestinationClient;
import nl.han.asd.submarine.models.routing.destination.DestinationRelay;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SymmetricEncryptionImplAes implements SymmetricEncryption {

    static final String INIT_VECTOR = "0705090625458532";

    // This is the version of the AES which is used for the Symmetric encryption. CBC Is the format of the block of AES.
    // All platforms use the PKCS5Padding of the CBC method. This goes against the SonarWay, because of vulnerabilities.
    // It is decided though that this way is save enough for the application
    static final String CIPHER_MODE = "AES/CBC/PKCS5Padding";

    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> formatTimestamp(json)).create();

    private LocalDateTime formatTimestamp(JsonElement json) {
        if(json.isJsonObject()) {
            JsonObject date = json.getAsJsonObject().get("date").getAsJsonObject();
            JsonObject time = json.getAsJsonObject().get("time").getAsJsonObject();
            return LocalDateTime.of(date.get("year").getAsInt(), date.get("month").getAsInt(), date.get("day").getAsInt(), time.get("hour").getAsInt(), time.get("minute").getAsInt(), time.get("second").getAsInt());
        } else {
            String timestamp = json.getAsString().substring(0, json.getAsString().lastIndexOf('.'));
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    @Override
    public String encrypt(Object contentToEncrypt, SecretKey secretKey) {
        if (secretKey == null)
            throw new IllegalArgumentException("The secretKey used for encryption can't be null.");
        if (contentToEncrypt == null)
            throw new IllegalArgumentException("The contentToEncrypt can't be null.");
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(CIPHER_MODE); // NOSONAR
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            String objectInString;
            if (!(contentToEncrypt instanceof String)) {
                objectInString = gson.toJson(contentToEncrypt);
            } else {
                objectInString = (String) contentToEncrypt;
            }

            byte[] encrypted = cipher.doFinal(objectInString.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to encrypt data: " + ex);
        }
    }


    @Override
    public String decrypt(String contentToDecrypt, String secretKey) throws IncorrectDecryptionKeyException {
        if (secretKey == null) throw new IllegalArgumentException("The secretKey used for decryption can't be null.");
        if (contentToDecrypt == null)
            throw new IllegalArgumentException("The contentToDecrypt to be decrypted can't be null.");
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(CIPHER_MODE); // NOSONAR
            cipher.init(Cipher.DECRYPT_MODE, convertKeyStringToSecretKeySpec(secretKey), iv);

            byte[] decodedContent = Base64.getDecoder().decode(contentToDecrypt);
            byte[] decodedObject = cipher.doFinal(decodedContent);

            return new String(decodedObject);
        } catch (BadPaddingException e) {
            throw new IncorrectDecryptionKeyException("This Secret key is incorrect, the content cannot be decrypted using this key.");
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new UnhandledEncryptionError("Unexpected error when attempting to decrypt data: " + e.getMessage(), e);
        }
    }

    @Override
    public SecretKey generateRandomSymmetricKey() {
        byte[] key = secureRandomCode(32);
        return new SecretKeySpec(key, "AES");
    }

    private byte[] secureRandomCode(int size) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] code = new byte[size];
        secureRandom.nextBytes(code);
        return code;
    }

    @Override
    public Onion encryptOnion(Path path, String receiverAlias, String encryptedMessage, String encryptedSymmetricKey, boolean isHTTPRequest) {
        if (path.getNodes().size() <= 1)
            throw new IllegalArgumentException("An onion requires at least 2 nodes to construct.");
        return constructOnionRecursively(true, new ArrayList<>(path.getNodes()), encryptedMessage, receiverAlias, null, encryptedSymmetricKey, isHTTPRequest);
    }

    @Override
    public SecretKey convertKeyStringToSecretKeySpec(String keyString) {
        byte[] keyInBytes = Base64.getDecoder().decode(keyString.getBytes());
        return new SecretKeySpec(keyInBytes, 0, keyInBytes.length, "AES");
    }

    private Onion constructOnionRecursively(boolean isFirst, List<Node> nodes, String data, String alias, Node previousNode, String encryptedSymmetricKey, boolean isHTTPRequest) {
        Node currentNode = nodes.remove(nodes.size() - 1);
        Destination destination;
        if (isFirst) {
            destination = new DestinationClient(alias, encryptedSymmetricKey);
        } else {
            destination = createDestinationRelay(previousNode);
        }

        data = encrypt(new Onion(destination, getCommandString(isFirst, isHTTPRequest), data), convertKeyStringToSecretKeySpec(currentNode.getKey()));

        if (nodes.isEmpty()) {
            return new Onion(createDestinationRelay(currentNode), getCommandString(isFirst, isHTTPRequest), data);
        } else {
            return constructOnionRecursively(false, nodes, data, null, currentNode, null, isHTTPRequest);
        }
    }

    private String getCommandString(boolean isFirst, boolean isHTTPRequest) {
        if (!isHTTPRequest) {
            return Command.RELAY.toString();
        } else {
            if (isFirst) {
                return Command.HTTP_REQUEST.toString();
            } else {
                return Command.HTTP_RELAY.toString();
            }
        }
    }

    private Destination createDestinationRelay(Node node) {
        var destinationRelay = (DestinationRelay) node.getDestination();
        return new DestinationRelay(destinationRelay.getHostname(), destinationRelay.getPort());
    }
}
