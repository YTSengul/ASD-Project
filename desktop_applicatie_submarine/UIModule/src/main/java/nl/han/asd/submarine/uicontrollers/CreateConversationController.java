package nl.han.asd.submarine.uicontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.han.asd.submarine.models.Contact;
import nl.han.asd.submarine.service.ContactService;
import nl.han.asd.submarine.service.ConversationService;
import org.controlsfx.control.CheckComboBox;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CreateConversationController {
    private static final Logger LOGGER = Logger.getLogger(CreateConversationController.class.getName());

    private static final int POPUP_MODAL_WIDTH = 500;
    private static final int POPUP_MODAL_HEIGHT = 333;

    private List<Contact> contacts = null;
    private List<String> contactsToAdd = List.of();
    private TextField conversationTitleTextField;
    private Text submitText;
    private Button submitButton;
    private Button cancelButton;

    private final ContactService contactService;
    private final ConversationService conversationService;

    public CreateConversationController(ContactService contactService, ConversationService conversationService) {
        this.contactService = contactService;
        this.conversationService = conversationService;
    }

    public void openChatModal() {
        Stage dialogStage = createPopupScene();

        Pane pane = createPane();
        pane.getChildren().addAll(
                getHeader(),
                getContactSelectBox(),
                getContactSelectBoxText(),
                getConversationTitleTextField(),
                getConversationTitleText(),
                getSubmitButton(),
                getCancelButton(dialogStage),
                submitText()
        );


        dialogStage.setScene(new Scene(pane));
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.showAndWait();
    }

    private Stage createPopupScene() {
        Stage popup = new Stage();
        popup.initModality(Modality.NONE);
        popup.setMinHeight(POPUP_MODAL_HEIGHT);
        popup.setMinWidth(POPUP_MODAL_WIDTH);
        popup.setResizable(false);
        popup.setTitle("Submarine Confidential Messaging Service - Create chat");

        popup.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images/logo.png"))));

        return popup;
    }

    private Pane createPane() {
        Pane pane = new Pane();
        pane.setMinHeight(POPUP_MODAL_HEIGHT);
        pane.setMinWidth(POPUP_MODAL_WIDTH);
        pane.setStyle("-fx-background-color: #4c5c68");

        return pane;
    }

    private CheckComboBox<String> getContactSelectBox() {
        contacts = contactService.getContacts();

        final ObservableList<String> contactAliases = FXCollections.observableArrayList();
        contacts.forEach(contact -> contactAliases.add(contact.getAlias()));

        final CheckComboBox<String> checkComboBox = new CheckComboBox<>(contactAliases.sorted());
        checkComboBox.setLayoutY(POPUP_MODAL_HEIGHT * 0.5);
        checkComboBox.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        checkComboBox.setPrefWidth(POPUP_MODAL_WIDTH * 0.8);
        checkComboBox.setPrefHeight(30);

        checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    contactsToAdd = (List<String>) c.getList();
                }
            }
        });
        return checkComboBox;
    }

    private Text getContactSelectBoxText() {
        final Text text = new Text("Participants:");
        text.setLayoutY(POPUP_MODAL_HEIGHT * 0.475);
        text.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        text.setStyle("-fx-font-size: 14; -fx-fill: #dcdddd; -fx-font-weight: bold;");
        return text;
    }

    private TextField getConversationTitleTextField() {
        conversationTitleTextField = new TextField();
        conversationTitleTextField.setStyle("-fx-background-color: #dcdddd;");
        conversationTitleTextField.setLayoutY(POPUP_MODAL_HEIGHT * 0.3);
        conversationTitleTextField.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        conversationTitleTextField.setPrefWidth(POPUP_MODAL_WIDTH * 0.8);
        conversationTitleTextField.setPrefHeight(30);

        return conversationTitleTextField;
    }

    private Text getConversationTitleText() {
        final Text text = new Text("Conversation title:");
        text.setLayoutY(POPUP_MODAL_HEIGHT * 0.265);
        text.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        text.setStyle("-fx-font-size: 14; -fx-fill: #dcdddd; -fx-font-weight: bold;");
        return text;
    }

    private AnchorPane getHeader() {
        final AnchorPane anchorPane = new AnchorPane();
        anchorPane.setMinWidth(POPUP_MODAL_WIDTH);
        anchorPane.setMinHeight(50);
        anchorPane.prefHeight(50);
        anchorPane.prefWidth(POPUP_MODAL_WIDTH);
        anchorPane.setStyle("-fx-background-color: #1985a1;");

        final Text text = new Text("Create conversation");
        text.setLayoutY(30);
        text.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        text.setStyle("-fx-font-weight: bold;-fx-font-size: 20;-fx-fill: white");
        anchorPane.getChildren().add(text);
        return anchorPane;
    }

    private Text submitText() {
        submitText = new Text();
        submitText.setStyle("-fx-font-size: 16;-fx-fill: #ff5c5c;-fx-font-weight: bold");
        submitText.setLayoutY(POPUP_MODAL_HEIGHT * 0.7);
        submitText.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        submitText.setText("Not all fields were filled in!");
        submitText.setVisible(false);
        return submitText;
    }

    private Button getSubmitButton() {
        submitButton = new Button();
        submitButton.setLayoutY(POPUP_MODAL_HEIGHT * 0.8);
        submitButton.setLayoutX(POPUP_MODAL_WIDTH * 0.525);
        submitButton.setPrefWidth(POPUP_MODAL_WIDTH * 0.375);
        submitButton.setPrefHeight(35);
        submitButton.setStyle("-fx-background-color: #1985a1; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.4) , 0, 0, 3, 3);");
        submitButton.setText("Create");

        submitButton.setOnAction(e -> submit());
        return submitButton;
    }

    private void submit() {
        submitText.setVisible(true);

        LOGGER.log(Level.INFO, "Attempting to create conversation.");
        if (conversationTitleTextField.getText().isBlank() || contactsToAdd.isEmpty()) {
            submitText.setVisible(true);
            submitText.setText("Not all fields were filled in!");
            LOGGER.log(Level.WARNING, "Creating conversation: Not all field were filled in.");
        } else {
            conversationService.createConversation(
                    conversationTitleTextField.getText(),
                    this.contacts.stream().filter(it -> contactsToAdd.contains(it.getAlias())).collect(Collectors.toList())
            );

            successfulCreatedCSS();
            ConversationController.getInstance().reload();

            LOGGER.log(Level.INFO, "Creating conversation: Conversation created successfully!");

        }
    }

    private Button getCancelButton(Stage stage) {
        cancelButton = new Button();
        cancelButton.setLayoutY(POPUP_MODAL_HEIGHT * 0.8);
        cancelButton.setLayoutX(POPUP_MODAL_WIDTH * 0.1);
        cancelButton.setPrefWidth(POPUP_MODAL_WIDTH * 0.375);
        cancelButton.setPrefHeight(35);
        cancelButton.setStyle("-fx-font-weight: bold; -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.4) , 0, 0, 2, 2);");
        cancelButton.setText("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        return cancelButton;
    }

    private void successfulCreatedCSS() {
        submitText.setStyle("-fx-font-size: 16;-fx-fill: #87cb4a;-fx-font-weight: bold");
        submitText.setText("Conversation created successfully!");
        submitText.setVisible(true);

        submitButton.setDisable(true);

        cancelButton.setText("Close");
    }
}
