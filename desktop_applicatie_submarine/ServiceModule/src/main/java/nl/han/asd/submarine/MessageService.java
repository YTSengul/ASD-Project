package nl.han.asd.submarine;


import com.google.gson.Gson;
import nl.han.asd.submarine.connection.ConnectionModule;
import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.encryption.DecryptToObjectUtil;
import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.message.MessageHandler;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.models.message.system.NewChat;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationClient;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.routing.RouteModule;
import nl.han.asd.submarine.service.ConversationService;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MessageService implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(MessageService.class.getName());

    @Inject
    private ConnectionModule connectionModule;

    @Inject
    private AsymmetricEncryption aSymmetricEncryption;

    @Inject
    private SymmetricEncryption symmetricEncryption;

    @Inject
    private RouteModule routeModule;

    @Inject
    private PersistenceModule persistenceModule;

    @Inject
    private ConversationService conversationService;


    @Override
    public void incomingMessage(Onion onion) {
        try {
            String privateKey = persistenceModule.getPrivateKeyOfUser();
            String encryptedSymmetricKey =
                    ((DestinationClient) onion.getDestination()).getEncryptedSymmetricKey();
            String decryptedSymmetricKey =
                    aSymmetricEncryption.decrypt(Base64.getDecoder().decode(encryptedSymmetricKey), privateKey);

            String decryptedJSONMessage = symmetricEncryption.decrypt(onion.getData(), decryptedSymmetricKey);
            Object object = DecryptToObjectUtil.toObject(decryptedJSONMessage);

            Class classFromGson = DecryptToObjectUtil.getClassForJson(decryptedJSONMessage);
            System.out.println(classFromGson.getSimpleName());
            System.out.println(" hierbo ven staat de naam");
            switch (classFromGson.getSimpleName()) {
                case "TextMessage":
                    persistenceModule.insertMessage((TextMessage) object);
                    return;
                case "ChunkMessage":
                    //persistenceModule.insertMessage((ChunkMessage) object);
                    return;
                case "RecipeMessage":
//                    decompressFileBasedOnFormat((RecipeMessage) object);
//                    persistenceModule.insertMessage((RecipeMessage) object);
                    return;
                case "NewChat":
                    System.out.println(" ik kom hier");
                    NewChat newChat = (NewChat) object;
                    handleIncomingNewChatMessage(newChat);
                    return;
                default:
                    System.out.println(classFromGson.getSimpleName());
                    throw new IllegalArgumentException("Unknown message received: " + classFromGson.toString());
            }
        } catch (IncorrectDecryptionKeyException e) {
            LOG.log(Level.SEVERE, "An exception was thrown while decrypting " +
                    "the received message.", e);
        }
    }

    private void handleIncomingNewChatMessage(NewChat newChat) {
        Conversation conversation = new Conversation(newChat.getConversationId(),
                newChat.getTitle(),
                newChat.getParticipants(),
                Arrays.asList(new TextMessage("SUBMARINE_SYSTEM", newChat.getConversationId(), LocalDateTime.now(), "Conversation created")));

        var userDataAlias = persistenceModule.getAlias();

        persistenceModule.insertContacts(newChat.getParticipants().stream()
                .filter(it -> !it.getAlias().equals(userDataAlias))
                .collect(Collectors.toList()));

        persistenceModule.insertConversation(conversation);
    }

    @Override
    public void sendTextMessage(String chatIdentifier, String message) {
        String currentUserAlias = persistenceModule.getAlias();
        TextMessage filledMessage = new TextMessage(currentUserAlias, chatIdentifier, message);
        this.sendMessage(filledMessage, persistenceModule.getChatParticipants(chatIdentifier));
        persistenceModule.insertMessage(filledMessage);
    }


    protected void sendMessage(Message message, List<Contact> participants) {
        participants.forEach(contact -> {
            String publicKey = contact.getPublicKey();
            SecretKey secretKey = symmetricEncryption.generateRandomSymmetricKey();
            String encryptedMessage = symmetricEncryption.encrypt(message, secretKey);
            String encryptedSymmetricKey =
                    Base64.getEncoder().encodeToString(aSymmetricEncryption.encrypt(Base64.getEncoder().encodeToString(secretKey.getEncoded()), publicKey));
            // As of now, length of path is always 3. Might be changed later.
            Onion onion = routeModule.makeOnion(encryptedMessage, contact.getAlias(), 3, encryptedSymmetricKey, false);
            connectionModule.sendMessage(onion);
        });
    }

    @Override
    public void handleIncomingMessage(Socket socket) {
        try (InputStream inputStream = socket.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (dataIsIncoming(inputStream, 200)) {
                while (inputStream.available() > 0) {
                    int data = inputStream.read();
                    outputStream.write(data);
                }
                byte[] array = outputStream.toByteArray();
                Onion onion = createOnionFromIncomingByteArray(array);
                incomingMessage(onion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Conversation getMessages(String chatIdentifier) {
        return persistenceModule.getConversation(chatIdentifier);
    }

    @Override
    public String getAlias() {
        return persistenceModule.getAlias();
    }

    public void initializeConnectionModule(int port) {
        connectionModule.listenForIncomingMessages(port);
    }

    private Onion createOnionFromIncomingByteArray(byte[] byteArray) {
        String onionString = new String(byteArray, StandardCharsets.UTF_8);
        return createOnionFromJsonString(onionString.replaceFirst("^[0-9]+", ""));
    }

    private Onion createOnionFromJsonString(String jsonString) {
        Gson gson = new Gson();
        Onion onion = gson.fromJson(jsonString, Onion.class);
        DestinationClient destinationClient =
                gson.fromJson(new JSONObject(jsonString).getJSONObject("destination").toString(), DestinationClient.class);
        onion.setDestination(destinationClient);
        return onion;
    }

    boolean dataIsIncoming(InputStream inputStream, long maxWaitTimeInMillis) throws IOException {
        long currentMillis = System.currentTimeMillis();
        while (currentMillis > System.currentTimeMillis() - maxWaitTimeInMillis) {
            if (inputStream.available() > 0) {
                return true;
            }
        }
        return false;
    }
}
