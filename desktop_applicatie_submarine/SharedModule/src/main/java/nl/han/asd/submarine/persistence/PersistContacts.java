package nl.han.asd.submarine.persistence;

import nl.han.asd.submarine.models.Contact;

import java.util.List;

public interface PersistContacts {
    void insertContact(Contact contact);

    void updateContact(Contact contact);

    void deleteContact(Contact contact);

    Contact getContact(int contactId);

    boolean hasContactWithAlias(String alias);

    Contact getContactByAlias(String alias);

    List<Contact> getContactList();

    void insertContacts(List<Contact> contacts);
}
