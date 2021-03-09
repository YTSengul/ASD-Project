package nl.han.asd.submarine;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.han.asd.submarine.chunking.FileSplitter;
import nl.han.asd.submarine.connection.ConnectionModule;
import nl.han.asd.submarine.connection.HandleChatterRequest;
import nl.han.asd.submarine.connection.SendMessage;
import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.encryption.SymmetricEncryption;
import nl.han.asd.submarine.message.MessageHandler;
import nl.han.asd.submarine.persistence.*;
import nl.han.asd.submarine.routing.RouteModule;
import nl.han.asd.submarine.service.ChatterService;
import nl.han.asd.submarine.service.ContactService;
import nl.han.asd.submarine.service.ConversationService;
import org.bson.Document;

public class BootstrapModule extends AbstractModule {

    @Override
    protected void configure() {

        // FileSplitter
        bind(FileSplitter.class).to(FileSplitterImpl.class);

        // MessageHandler
        bind(MessageHandler.class).to(MessageService.class);

        // Encryption
        bind(new TypeLiteral<SymmetricEncryption>() {
        }).to(new TypeLiteral<SymmetricEncryptionImplAes>() {
        });
        bind(new TypeLiteral<AsymmetricEncryption>() {
        }).to(new TypeLiteral<AsymmetricEncryptionImplRsa>() {
        });
        bind(AsymmetricEncryption.class).to(AsymmetricEncryptionImplRsa.class);
        bind(SymmetricEncryption.class).to(SymmetricEncryptionImplAes.class);

        // Persistence
        bind(PersistenceModule.class).to(PersistenceModuleImpl.class);
        bind(PersistContacts.class).to(PersistContactsImpl.class);
        bind(PersistConversations.class).to(PersistConversationsImpl.class);
        bind(PersistMessages.class).to(PersistMessagesImpl.class);
        bind(PersistUserData.class).to(PersistUserDataImpl.class);

        // Connection
        bind(ConnectionModule.class).to(ConnectionModuleImpl.class);
        bind(SendMessage.class).to(SendMessageImpl.class);
        bind(ServerSocketFactory.class).to(ServerSocketFactoryImpl.class);
        bind(HandleChatterRequest.class).to(HandleChatterRequestImpl.class);

        // Service
        bind(ChatterService.class).to(ChatterServiceImpl.class);
        bind(ContactService.class).to(ContactServiceImpl.class);
        bind(MessageHandler.class).to(MessageService.class);
        bind(ConversationService.class).to(ConversationServiceImpl.class);

        // Route
        bind(RouteModule.class).to(RouteModuleImpl.class);

        MongoClient client = new MongoClient("localhost", 27017); // NOSONAR
        MongoDatabase database = client.getDatabase("submarine");
        bind(MongoDatabase.class).toInstance(database);
        bind(new TypeLiteral<MongoCollection<Document>>() {
        }).annotatedWith(Names.named(DatabaseCollections.CONTACT.getValue())).toInstance(database.getCollection(DatabaseCollections.CONTACT.getValue()));
        bind(new TypeLiteral<MongoCollection<Document>>() {
        }).annotatedWith(Names.named(DatabaseCollections.CONVERSATION.getValue())).toInstance(database.getCollection(DatabaseCollections.CONVERSATION.getValue()));
        bind(new TypeLiteral<MongoCollection<Document>>() {
        }).annotatedWith(Names.named(DatabaseCollections.USER_DATA.getValue())).toInstance(database.getCollection(DatabaseCollections.USER_DATA.getValue()));


        bind(Integer.class).annotatedWith(Names.named("clientPort")).toInstance(25010);
        bind(String.class).annotatedWith(Names.named("encryptedSymmetricKey")).toInstance("9H+5ObOd2IO/KQcGTTnKR5h3F1DrWFjXt0oud+zlL0Q=");
        bind(String.class).annotatedWith(Names.named("chatterServerIp")).toInstance("94.124.143.166");
        bind(String.class).annotatedWith(Names.named("nodeDirectoryServerIp")).toInstance("94.124.143.165");
    }

}
