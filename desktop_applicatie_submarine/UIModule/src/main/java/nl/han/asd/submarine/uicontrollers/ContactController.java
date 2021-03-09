package nl.han.asd.submarine.uicontrollers;

import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import nl.han.asd.submarine.exception.DuplicateContactException;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.service.ContactService;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class ContactController extends ControllerBase {
    private static final Logger LOGGER = Logger.getLogger(ContactController.class.getName());
    @Inject
    private final ContactService contactService;
    @FXML
    private TextField addContactAliasField;
    @FXML
    private TextField addContactPublicKeyField;
    @FXML
    private Text addContactText;
    @FXML
    private Button addContactButton;
    @FXML
    private AnchorPane addContactAnchorPane;

    public ContactController() {
        super();
        contactService = injector.getInstance(ContactService.class);
    }

    @FXML
    public void addContactAction(ActionEvent event) {
        LOGGER.log(INFO, "Attempt made to add a contact.");
        resizeAccordion();

        if (addContactAliasField.getText().isBlank() || addContactPublicKeyField.getText().isBlank()) {
            LOGGER.log(WARNING, "Adding contact failed! Not all fields were filled in.");
            setAddContactText("Not all fields were filled in.");
        } else {
            Contact contact = new Contact(addContactPublicKeyField.getText(), addContactAliasField.getText());
            try {
                contactService.addContact(contact);
            } catch (DuplicateContactException e) {
                setAddContactText("Already got a contact with the alias '" + contact.getAlias() + "'");
                return;
            }
            resetFieldsAndGiveSuccessText();
        }
    }

    private void resetFieldsAndGiveSuccessText() {
        setAddContactText("Contact has been added to your contacts.");
        addContactAliasField.undo();
        addContactPublicKeyField.undo();
        LOGGER.log(INFO, "Successfully added contact to chatter's contacts."); // NOSONAR
    }

    private void setAddContactText(String text) {
        addContactText.setText(text);
        addContactText.setVisible(true);
    }

    private void resizeAccordion() {
        addContactText.setText("Not all fields were filled in.");
        addContactButton.setLayoutY(133.0);
        addContactAnchorPane.setPrefHeight(170.0);
    }

}
