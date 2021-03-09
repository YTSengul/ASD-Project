package nl.han.asd.submarine.uicontrollers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import nl.han.asd.submarine.BootstrapModule;
import nl.han.asd.submarine.service.ContactService;
import nl.han.asd.submarine.service.ConversationService;
import nl.han.asd.submarine.uicontrollers.chat.ChatController;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

public class ConversationController {
    @FXML
    private VBox conversationsBox;

    @Inject
    private final ConversationService conversationService;
    @Inject
    private final ContactService contactService;

    private static final String DEFAULT_CLASS = "defaultConversation";
    private static final String SELECTED_CLASS = "selectedConversation";

    private Map<String, String> conversations;

    private static ConversationController instance;

    public static ConversationController getInstance() {
        return instance;
    }

    public ConversationController() {
        instance = this;
        Injector injector = Guice.createInjector(new BootstrapModule());
        conversationService = injector.getInstance(ConversationService.class);
        contactService = injector.getInstance(ContactService.class);
    }

    public void initialize() {
        conversations = conversationService.getConversations();
        loadConversations();
    }

    private void loadConversations() {
        final int DEFAULT_BUTTON_HEIGHT = 50;
        conversations.forEach((id, title) -> {
            JFXButton button = new JFXButton();

            var currentIndex = conversationsBox.getChildren().size();
            button.setLayoutY(DEFAULT_BUTTON_HEIGHT + (DEFAULT_BUTTON_HEIGHT * currentIndex));
            button.setPrefWidth(380.0);
            button.setPrefHeight(40.0);

            button.setText(title);

            button.setStyle(
                    "-fx-alignment: " + Pos.CENTER_LEFT + ";" +
                            "-fx-border-radius: 0;" +
                            "-fx-background-radius:0;" +
                            "-fx-font-size:15px;"
            );
            button.getStyleClass().add(DEFAULT_CLASS);

            button.setButtonType(JFXButton.ButtonType.RAISED);
            button.setOnAction(e -> setActiveConversation(id));
            button.setId(id);

            conversationsBox.getChildren().add(button);
        });
    }

    public void reload() {
        conversations = conversationService.getConversations();
        if (conversations.size() != conversationsBox.getChildren().size()) {
            conversationsBox.getChildren().clear();
            loadConversations();
            if (isAConversationActive()) {
                setActiveConversation(getActiveConversationId());
            }
        }
    }

    @FXML
    private void openChatModal(ActionEvent event) {
        new CreateConversationController(contactService, conversationService).openChatModal();
    }

    private void setActiveConversation(String id) {
        ChatController.getInstance().setActiveConversation(Collections.singletonMap(id, conversations.get(id)));
        conversationsBox.getChildren().forEach(conversation -> {
            conversation.getStyleClass().remove(SELECTED_CLASS);
            if (conversation.getId().equals(id)) {
                conversation.getStyleClass().add(SELECTED_CLASS);
            }
        });
    }

    private String getActiveConversationId() {
        return ChatController.getInstance().getActiveConversationId();
    }

    private boolean isAConversationActive() {
        return ChatController.getInstance().isAConversationActive();
    }
}