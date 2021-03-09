package nl.han.asd.submarine.uicontrollers.chat;

import com.google.inject.Inject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import nl.han.asd.submarine.MessageService;
import nl.han.asd.submarine.message.MessageHandler;
import nl.han.asd.submarine.models.Conversation;
import nl.han.asd.submarine.models.message.FileMessage;
import nl.han.asd.submarine.models.message.Message;
import nl.han.asd.submarine.models.message.TextMessage;
import nl.han.asd.submarine.service.ConversationService;
import nl.han.asd.submarine.uicontrollers.ControllerBase;
import nl.han.asd.submarine.uicontrollers.ConversationController;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.han.asd.submarine.uicontrollers.chat.ChatUtil.getTimeOfLocalDateTime;

public class ChatController extends ControllerBase {
    private static final Logger LOG = Logger.getLogger(ChatController.class.getName());
    private static ChatController instance;
    final FileChooser fileChooser = new FileChooser();

    @Inject
    private final MessageHandler messageService;
    private final AtomicInteger amountAdded = new AtomicInteger();
    @FXML
    public TextField chatField;
    @FXML
    private VBox chatContainer;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Text conversationTitle;

    private List<SpeechBox> bubbles;

    private Map<String, String> activeConversation = null;


    @FXML
    private SplitPane splitPane;

    @Inject
    private final ConversationService conversationService;

    public ChatController() {
        super();
        instance = this;
        messageService = injector.getInstance(MessageService.class);
        conversationService = injector.getInstance(ConversationService.class);
        instance = this;
    }

    public static ChatController getInstance() {
        return instance;
    }

    @FXML
    private void sendChatAction(ActionEvent event) {
        if (!chatField.getText().isBlank()) {
            LOG.log(Level.INFO, "Chat message: {0}", chatField.getText());
            messageService.sendTextMessage(getActiveConversationId(), chatField.getText());
            chatField.setText("");
            showMessages();
        }
    }

    @FXML
    private void sendFileAction(ActionEvent event) {
        List<File> uploadedFileList = fileChooser.showOpenMultipleDialog(null);

        if (uploadedFileList != null) {
            for (File uploadedFile : uploadedFileList) {
                //TODO: Make real implementation for file sending: ODZKJZ-409
                LOG.log(Level.INFO, "Uploaded file: {0}", uploadedFile);
            }
        }
    }

    public void initialize() {
        disableResizeSplitPane();
        showMessages();

        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ConversationController.getInstance().reload();
            showMessages();
        }));
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();
    }

    private void disableResizeSplitPane() {
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.positionProperty().addListener((observable, oldvalue, newvalue) -> divider.setPosition(0.3807615230460922));
    }

    private void showMessages() {
        if (activeConversation != null) {
            Conversation conversation = messageService.getMessages(getActiveConversationId());
            String alias = messageService.getAlias();
    final String[] currentDate = {null};
        if ((chatContainer.getChildren().size() - amountAdded.get())!= conversation.getMessages().size()) {
                amountAdded.set(0);chatContainer.getChildren().clear();
                conversation.getMessages().forEach(message -> {
                        if (currentDate[0] == null || !currentDate[0].equals(localDateTimeToStringDate(message.getTimestamp()))) {
                            currentDate[0] = localDateTimeToStringDate(message.getTimestamp());
                            addDateBubble(currentDate[0]);
                            amountAdded.getAndIncrement();
                        }
                        addMessageBubble(alias, message);
                    });
            }
        }
    }

    private void addDateBubble(String date) {
        SpeechBox bubble = new SpeechBox(convertToDay(date), SpeechDirection.CENTER);
        bubble.setPadding(new Insets(2, 0, 2, 0));
        chatContainer.getChildren().add(bubble);
    }

    private String convertToDay(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDate today = LocalDate.now();
        if (today.isEqual(localDate)) {
            return "Today";
        } else if (today.minusDays(1).isEqual(localDate)) {
            return "Yesterday";
        } else if (today.minusDays(5).isBefore(localDate)) {
            return localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        } else {
            return date;
        }
    }

    private String localDateTimeToStringDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void addMessageBubble(String alias, Message message) {
        MessageBox bubble = null;
        if (message instanceof TextMessage) {
            var textMessage = (TextMessage) message;
            if (textMessage.getSender().equals("SUBMARINE_SYSTEM")) {
                bubble = getSpeechBoxFromSystem(textMessage);
            } else {
                bubble = getSpeechBoxFromMessage(alias, textMessage);
            }
        } else if (message instanceof FileMessage) {
            bubble = getFileBoxFromMessage(alias, (FileMessage) message);
        }
        if (bubble != null) {
            bubble.setPadding(new Insets(2, 0, 2, 0));
            chatContainer.getChildren().add(bubble);
        }
        chatScrollPane.setVvalue(chatScrollPane.getVmax());
    }

    private FileBox getFileBoxFromMessage(String alias, FileMessage message) {
        File file = new File(message.getPath());
        FileBox bubble;

        if (message.getSender().equals(alias)) {
            bubble = new FileBox(file.getName(), file.length(), message.getTimestamp(), SpeechDirection.RIGHT);
        } else {
            bubble = new FileBox(file.getName(), message.getSender(), file.length(), message.getTimestamp(), SpeechDirection.LEFT);
        }
        bubble.setOnMouseClicked((args) -> tryToOpenFile(file));
        return bubble;
    }

    private void tryToOpenFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SpeechBox getSpeechBoxFromMessage(String alias, TextMessage message) {
        if (message.getSender().equals(alias)) {
            return new SpeechBox(
                    "(" + getTimeOfLocalDateTime(message.getTimestamp()) + ")" + " \n" +
                            message.getMessage(), SpeechDirection.RIGHT
            );
        } else {
            return new SpeechBox(
                    message.getSender() +
                            " (" + getTimeOfLocalDateTime(message.getTimestamp()) + ")" + " \n" +
                            message.getMessage(), SpeechDirection.LEFT
            );
        }
    }

    private SpeechBox getSpeechBoxFromSystem(TextMessage message) {
        return new SpeechBox(message.getMessage(), SpeechDirection.CENTER);
    }

    public String getActiveConversationId() {
        return activeConversation.entrySet().iterator().next().getKey();
    }

    public boolean isAConversationActive() {
        return activeConversation != null;
    }

    public void setActiveConversation(Map<String, String> activeConversation) {
        this.activeConversation = activeConversation;
        conversationTitle.setText(activeConversation.entrySet().iterator().next().getValue());
        showMessages();
    }
}
