package nl.han.asd.submarine;

import com.google.gson.Gson;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.mongodb.client.MongoCollection;
import nl.han.asd.submarine.connection.ConnectionModule;
import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.encryption.IncorrectDecryptionKeyException;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.models.routing.destination.DestinationClient;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.routing.RouteModule;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    private Injector injector;

    ConnectionModule connectionModuleMock;
    AsymmetricEncryption aSymmetricEncryptionMock;
    SymmetricEncryption symmetricEncryptionMock;
    RouteModule routeModuleMock;
    PersistenceModule persistenceModuleMock;

    MessageService messageService;

    @Mock
    MongoCollection<Document> collectionMock;

    @BeforeEach
    public void setup() {
        class TestBootstrapModule extends AbstractModule {
            MessageService messageService = new MessageService();
            ConnectionModule connectionModuleMock = mock(ConnectionModule.class);
            AsymmetricEncryption aSymmetricEncryptionMock = mock(AsymmetricEncryption.class);
            SymmetricEncryption symmetricEncryptionMock = mock(SymmetricEncryption.class);
            RouteModule routeModuleMock = mock(RouteModule.class);
            PersistenceModule persistenceModuleMock = mock(PersistenceModule.class);

            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(ConnectionModule.class).toInstance(connectionModuleMock);
                bind(new TypeLiteral<AsymmetricEncryption>() {
                }).toInstance(aSymmetricEncryptionMock);
                bind(new TypeLiteral<SymmetricEncryption>() {
                }).toInstance(symmetricEncryptionMock);
                bind(RouteModule.class).toInstance(routeModuleMock);
                bind(PersistenceModule.class).toInstance(persistenceModuleMock);
                bind(new TypeLiteral<MongoCollection<Document>>() {
                }).annotatedWith(Names.named("conversation")).toInstance(collectionMock);
            }
        }
        this.injector =
                Guice.createInjector(Modules.override(new BootstrapModule()).with(new TestBootstrapModule()));

        messageService = injector.getInstance(MessageService.class);

        connectionModuleMock = injector.getInstance(ConnectionModule.class);
        aSymmetricEncryptionMock = injector.getInstance(new Key<>(){});
        symmetricEncryptionMock = injector.getInstance(SymmetricEncryption.class);
        routeModuleMock = injector.getInstance(RouteModule.class);
        persistenceModuleMock = injector.getInstance((PersistenceModule.class));
    }

    @Test
    void sendTextMessageSuccessTest() {
        // setup
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        PublicKey publicKeyMock = mock(PublicKey.class);
        SecretKey secretKeyMock = mock(SecretKey.class);
        byte[] encodedArray = new byte[]{80, 53, -63, 28, 0, -81, 68, -10, 111, 54, 127, 109, -43, 9, -84, 65, -97, -83, -78, 98, 13, -80, 99, 38, -27, -49, 121, 48, 104, -5, -100, -35};

        String expectedAlias = "TestAlias";
        String expectedIdentifier = "testIdentifier";
        String expectedMessage = "testMessage";
        String expectedEncryptedContent = "GoodEncryptedContent";
        Onion expectedOnion = mock(Onion.class);
        String expectedPublicKey = "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud";
        byte[] expectedEncryptedKey = "fakeKey".getBytes();

        List<Contact> expectedContacts = new ArrayList<>();
        expectedContacts.add(new Contact(expectedPublicKey, "Tuna Eagle"));
        expectedContacts.add(new Contact(expectedPublicKey, "Smelly Seal"));
        expectedContacts.add(new Contact(expectedPublicKey, "Waiting Whale"));

        when(persistenceModuleMock.getAlias()).thenReturn(expectedAlias);
        when(persistenceModuleMock.getChatParticipants(expectedIdentifier)).thenReturn(expectedContacts);
        when(aSymmetricEncryptionMock.encrypt(anyString(), any())).thenReturn(expectedEncryptedKey);
        when(symmetricEncryptionMock.generateRandomSymmetricKey()).thenReturn(secretKeyMock);
        when(secretKeyMock.getEncoded()).thenReturn(encodedArray);
        when(symmetricEncryptionMock.encrypt(any(TextMessage.class), any(SecretKey.class))).thenReturn(expectedEncryptedContent);
        when(routeModuleMock.makeOnion(any(), anyString(), anyInt(), any(), anyBoolean())).thenReturn(expectedOnion);
        // run
        messageService.sendTextMessage(expectedIdentifier, expectedMessage);
        // check
        verify(persistenceModuleMock, times(1)).getAlias();
        verify(persistenceModuleMock).insertMessage(messageCaptor.capture());
        TextMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getConversationId()).isEqualTo(expectedIdentifier);
        assertThat(capturedMessage.getSender()).isEqualTo(expectedAlias);
        assertThat(capturedMessage.getMessage()).isEqualTo(expectedMessage);
        verify(persistenceModuleMock, times(1)).getChatParticipants(expectedIdentifier);
        verify(symmetricEncryptionMock, times(3)).generateRandomSymmetricKey();
        verify(symmetricEncryptionMock, times(3)).encrypt(capturedMessage, secretKeyMock);
        verify(routeModuleMock, times(3)).makeOnion(any(), anyString(), anyInt(), any(), anyBoolean());
        verify(connectionModuleMock, times(3)).sendMessage(expectedOnion);
    }

    @Test
    void sendTextMessageEmptyChatTest() {
        // Expected behaviour: Everything passes, but no messages are send to the network.
        // setup
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        PublicKey publicKeyMock = mock(PublicKey.class);

        String expectedAlias = "TestAlias";
        String expectedIdentifier = "testIdentifier";
        String expectedMessage = "testMessage";
        String expectedEncryptedContent = "GoodEncryptedContent";
        Onion expectedOnion = mock(Onion.class);
        String expectedKeyString = "9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q=";

        List<Contact> expectedContacts = Collections.emptyList();

        when(persistenceModuleMock.getAlias()).thenReturn(expectedAlias);
        when(persistenceModuleMock.getChatParticipants(expectedIdentifier)).thenReturn(expectedContacts);
        when(persistenceModuleMock.getChatParticipants(expectedIdentifier)).thenReturn(expectedContacts);
        // run
        messageService.sendTextMessage(expectedIdentifier, expectedMessage);
        // check
        verify(persistenceModuleMock, times(1)).getAlias();
        verify(persistenceModuleMock).insertMessage(messageCaptor.capture());
        TextMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getConversationId()).isEqualTo(expectedIdentifier);
        assertThat(capturedMessage.getSender()).isEqualTo(expectedAlias);
        assertThat(capturedMessage.getMessage()).isEqualTo(expectedMessage);
        verify(persistenceModuleMock, times(1)).getChatParticipants(expectedIdentifier);
        verify(symmetricEncryptionMock, times(0)).generateRandomSymmetricKey();
        verify(symmetricEncryptionMock, times(0)).encrypt(any(), any());
        verify(routeModuleMock, times(0)).makeOnion(any(), anyString(), anyInt(), any(), anyBoolean());
        verify(connectionModuleMock, times(0)).sendMessage(expectedOnion);
    }

    @Test
    void incomingMessageIsDecryptedSuccessfully() throws IncorrectDecryptionKeyException {
        final String PRIVATE_KEY_STRING = "PRIVATEKEYSTRING";
        final String DECRYPTED_SYMM_KEY = "DECRYPTEDSYMMKEY";
        final String ENCRYPTED_SYMM_KEY = "ENCRYPTEDSYMMKEY";
        Message message = new TextMessage("SENDER", "CHAT-01", "Bericht");
        Onion onion = new Onion(new DestinationClient("TestAlias",ENCRYPTED_SYMM_KEY), "RELAY", "TEST");

        when(persistenceModuleMock.getPrivateKeyOfUser()).thenReturn(PRIVATE_KEY_STRING);
        when(aSymmetricEncryptionMock.decrypt(any(), any())).thenReturn(DECRYPTED_SYMM_KEY);
        when(symmetricEncryptionMock.decrypt(onion.getData(), DECRYPTED_SYMM_KEY)).thenReturn(new Gson().toJson(message));
        messageService.incomingMessage(onion);

        verify(persistenceModuleMock, times(1)).insertMessage(any());
    }

    @Test
    void returnTrueIfDataIsIncoming() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[100]);
        assertThat(messageService.dataIsIncoming(inputStream, 100)).isTrue();
    }

    @Test
    void returnFalseIfNoDataIsIncoming() throws IOException {
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
        assertThat(messageService.dataIsIncoming(inputStream, 100)).isFalse();
    }
}
