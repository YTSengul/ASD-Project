package nl.han.asd.submarine;

import com.google.inject.Inject;
import nl.han.asd.submarine.exception.DuplicateContactException;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.service.ContactService;

import java.util.List;

public class ContactServiceImpl implements ContactService {

    @Inject
    PersistenceModule persistenceModule;

    @Override
    public void addContact(Contact contact) {
        if (persistenceModule.hasContactWithAlias(contact.getAlias())) throw new DuplicateContactException();

        persistenceModule.insertContact(contact);
    }

    @Override
    public List<Contact> getContacts() {
        return persistenceModule.getContactList();
    }

    @Override
    public void insertContacts(List<Contact> contacts) {
        persistenceModule.insertContacts(contacts);
    }
}
