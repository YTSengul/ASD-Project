package nl.han.asd.submarine.uicontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import nl.han.asd.submarine.uicontrollers.chat.ChatController;

public class EmojiController extends ControllerBase {
    private static final String[] EMOJI_CODE_POINTS = {"😄", "🖤", "💩", "🔥", "👍", "👎"};

    @FXML
    private void insertEmoji(ActionEvent event) {
        MenuItem target = (MenuItem) event.getSource();
        int emojiId = Integer.parseInt(target.getId().split("emoji")[1]);
        ChatController chatController = ChatController.getInstance();

        chatController.chatField.setText(chatController.chatField.getText() + EMOJI_CODE_POINTS[emojiId]);
    }
}
