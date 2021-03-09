package nl.han.asd.submarine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.client.MongoCollection;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistContacts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class PersistContactsImpl implements PersistContacts {

    @Inject
    @Named(DatabaseCollections.Constants.CONTACT)
    private MongoCollection<Document> collection;

    // COLLECTION FIELDS
    private static final String ALIAS = "alias";
    private static final String PUBLICKEY = "publicKey";

    public void insertContact(Contact contact) {
        Document contactDocument = new Document();
        contactDocument.append(ALIAS, contact.getAlias());
        contactDocument.append(PUBLICKEY, contact.getPublicKey());

        collection.insertOne(contactDocument);
    }

    public void updateContact(Contact contact) {
        throw new UnsupportedOperationException();
    }

    public void deleteContact(Contact contact) {
        throw new UnsupportedOperationException();
    }

    public Contact getContact(int contactId) {
        return new Contact("test", "test");
    }

    public boolean hasContactWithAlias(String alias) {
        return collection.find(eq(ALIAS, alias)).first() != null;
    }

    @Override
    public Contact getContactByAlias(String alias) {
        Document document = collection.find(eq(ALIAS, alias)).first();
        try {
            return new Contact(document.getString(PUBLICKEY), document.getString(ALIAS));
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Contact with alias " + alias + " was not found");
        }
    }

    public List<Contact> getContactList() {
        List<Document> documentList = new ArrayList<>();
        return collection.find()
                .into(documentList)
                .stream()
                .map(m -> new Contact(m.getString(PUBLICKEY), m.getString(ALIAS)))
                .collect(Collectors.toList());
    }

    @Override
    public void insertContacts(List<Contact> contacts) {
        var uniqueContacts = contacts.stream()
                .filter(it -> !getContactList().contains(it))
                .map(it -> new Document(ALIAS, it.getAlias()).append(PUBLICKEY, it.getPublicKey()))
                .collect(Collectors.toList());
        if (uniqueContacts.isEmpty()) return;
        collection.insertMany(uniqueContacts);
    }
}
