package nl.han.asd.submarine.service;

import nl.han.asd.submarine.models.Contact;

import java.util.List;

public interface ContactService {

    void addContact(Contact contact);
    List<Contact> getContacts();

    void insertContacts(List<Contact> contacts);

}
