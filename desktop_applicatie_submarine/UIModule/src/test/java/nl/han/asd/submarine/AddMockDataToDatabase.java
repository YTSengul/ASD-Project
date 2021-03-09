package nl.han.asd.submarine;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.han.asd.submarine.message.MessageType;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddMockDataToDatabase {
    // Run this file and presto, your database is filled with garbage
    // data to do some testing with. How nice!
    public static void main(String[] args) {
        var database = new MongoClient("localhost", 27017);
        AddMockDataToDatabase.start(database.getDatabase("submarine"));
        database.close();
    }

    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxcEUV+Z/v/WDGG6HfJByfzN2N+pBKUbccgFv/gIVKYguoVswR0chb/Q0Bst2fg8xV/bnWKIWAx+K/e2VrI69KwTIK1ub4P/5OIf0AbPZFaiYF24AKZVhwNMz0tYaaKD19K0xf3plsBSRmdVWIDLs6B6EOT608TDJSCrvifI7Apud8YX7LWJNMVIFHcey+pijqqP9m1jGxLhCDK/a/S8Y4eKXsjuMpXZymmHJe5gHXb6yrFeVIiTm3UA9WL5VqT9SzT2x2aTFEkjPyNxPjp5HAyKkk5o+8rfWZ+yD/EXB7XPxlU0qCQ2ibJVug4E8YW0mAp1m9tEiLwx3bJf66EncyQIDAQAB";
    private static final String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDFwRRX5n+/9YMYbod8kHJ/M3Y36kEpRtxyAW/+AhUpiC6hWzBHRyFv9DQGy3Z+DzFX9udYohYDH4r97ZWsjr0rBMgrW5vg//k4h/QBs9kVqJgXbgAplWHA0zPS1hpooPX0rTF/emWwFJGZ1VYgMuzoHoQ5PrTxMMlIKu+J8jsCm53xhfstYk0xUgUdx7L6mKOqo/2bWMbEuEIMr9r9Lxjh4peyO4yldnKaYcl7mAddvrKsV5UiJObdQD1YvlWpP1LNPbHZpMUSSM/I3E+OnkcDIqSTmj7yt9Zn7IP8RcHtc/GVTSoJDaJslW6DgTxhbSYCnWb20SIvDHdsl/roSdzJAgMBAAECggEBALqKmOMWBroQITrhwKKrK6Z75+905UTHmFleq8MjOc00ri/9cfw6x/rwlEc4lKzzLeu5d5/vPe5ySc2g2UiuWRmEaEImJ1PGgtRlwrH8cjHxGI85b6PKSyz2zfL7jmvWMk09Nuz2i4fezz4atSSohTus1bOeSFcgWy7deZpZynp4oN2njhCnVqXZBs1WG7WiVZTsooi46MVTvqqUwDN0ttXEX/HIR62mgK91vPIbjmGpJyhb74ThSXG5pPIgv2ZwCwMz5/oYwtmzfc5PlBu794TKbkDstPcjtdXw7Bzgc17xy3/pISdLTZNxDCLqJCLJepxApn6fwv9XP4M3zpaSpNECgYEA9vsM0u97RB5KNNEBImAiUVGG3kOf7+hOHlTBGTLB89Q9UsMxsdSGqKU+zXB8TugQGSbv0wR5YkhKbnZkhRL+EBpb+aGQWfhK0Ien/6dx61Y2/aSv6DkF1pbAzsYeP4KmYqxU/s/65h5iieoL98logr+Zvz4B/alrKz4MwOhsRf0CgYEAzPnTYub05fIQwni9qmpLRnRAvcZ+6cJtgqQYeFZHwBtAgw9x50fj0v5MhWecmkVgDM/3sPPH0whoCvRMlQfNmMSmqgtg/mUBN3gsoxtJjAB2BNQk4V/jDKSl2JfM9ioZboU8OcfpGCZE0SDxF2/IH7QmLOyGXtn9P7LuDmGBRb0CgYEAjvjReaIsZ0hpiJ2c3HtuIkjhm/Tltwlxf1ls1tttL2sjfK+GKv1uXl4aNu7w5vSrrQO/PRaP9fcHL17V3bsa29OGfsGqQIrdNE3zUKlr5uHnv3kAY5vu50QppQlHJVjKXMCKC1/2TBQ20sNr3ir3lwi3Rd/tYxwZ79RhqaJiWzkCgYA+gxZvvEYF9swbsx0M4vvwEbZI6kd8XRpu1ELOOGoLcYQ8NDKluWwGxh1qNg4xkmTXo3lD0yUm/BJW/Oh6LvYJ2DeH9o5vJzi2xQS//atxchaMJHLW9RpWmcQmiS0uCerVpPaYHJq/DbTBHs3IOwQKfzOBUeUs+khc6+HkKuJ09QKBgFylKVLuMLwVehNOsMHOM6GyocZ4b+OSsyRwdcYu850pJABhlo5QGzkjUwaka5VApim64rCsROKXdCXs3KrkfMonaYLv8sGq4eGaKxIIXTZgpYaO8V+sBPndsGC9z9xDmVSM33khYXgYzgarfZmN/64GkjPsH2aYQCW9Y21yW7AC";


    public static void start(MongoDatabase databaseInstance) {
        // WARNING: This will drop the database! Useful for getting a clean
        // slate,
        // not so useful for when you have important stuff in the database.
        databaseInstance.drop();


        List<Contact> contactlist = new ArrayList<>();
        contactlist.add(new Contact(publicKey, "Tuna Eagle"));
        contactlist.add(new Contact(publicKey, "Smelly Seal"));
        contactlist.add(new Contact(publicKey, "Waiting Whale"));

        List<Message> messageList = new ArrayList<>();
        messageList.add(
                new TextMessage(
                        contactlist.get(0).getAlias(),
                        "testConvoId",
                        LocalDateTime.now().minusDays(3),
                        "Hello There")
        );
        messageList.add(
                new TextMessage(
                        contactlist.get(2).getAlias(),
                        "testConvoId",
                        LocalDateTime.now().minusDays(3),
                        "Hello You")
        );
        messageList.add(
                new TextMessage(
                        contactlist.get(1).getAlias(),
                        "testConvoId",
                        LocalDateTime.now().minusHours(5),
                        "Hello Me")
        );
        messageList.add(
                new TextMessage(
                        contactlist.get(1).getAlias(),
                        "testConvoId",
                        LocalDateTime.now().minusHours(3),
                        "Hello Us")
        );
        messageList.add(
                new TextMessage(
                        contactlist.get(0).getAlias(),
                        "testConvoId",
                        LocalDateTime.now(),
                        "Hello World")
        );

        Conversation conversation = new Conversation("testConvoId", "ASD-B Rocks", contactlist, messageList);
        MongoCollection<Document> collection = databaseInstance.getCollection(DatabaseCollections.CONTACT.getValue());

        List<Document> contacts = new ArrayList<>();
        List<Document> messages = new ArrayList<>();
        for (Contact contact : conversation.getParticipants()) {
            contacts.add(new Document("alias", contact.getAlias()).append("publicKey", contact.getPublicKey())            );
        }
        collection.insertMany(contacts);

        for (Message message : conversation.getMessages()) {
            TextMessage textMessage = (TextMessage) message;
            messages.add(
                    new Document("messageType", MessageType.TEXT.toString())
                            .append("alias", textMessage.getSender())
                            .append("conversationId", message.getConversationId())
                            .append("timeStamp", message.getTimestamp())
                            .append("message", ((TextMessage) message).getMessage())
            );
        }

        collection = databaseInstance.getCollection(DatabaseCollections.CONVERSATION.getValue());
        Document conversationDocument = new Document("_id", conversation.getId())
                .append("participants", contacts)
                .append("messages", messages);
        collection.insertOne(conversationDocument);

        // Insert an alias
        collection = databaseInstance.getCollection(DatabaseCollections.USER_DATA.getValue());
        Document doc = new Document("alias", "Melting Martian");
        doc.put("publicKey", publicKey);
        doc.put("privateKey", privateKey);
        collection.insertOne(doc);

    }
}
