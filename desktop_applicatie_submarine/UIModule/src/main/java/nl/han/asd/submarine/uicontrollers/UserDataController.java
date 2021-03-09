package nl.han.asd.submarine.uicontrollers;

import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import nl.han.asd.submarine.service.ChatterService;

import java.util.logging.Logger;

public class UserDataController extends ControllerBase {
    private static final Logger LOGGER = Logger.getLogger(ContactController.class.getName());

    private ChatterService chatterService;

    public UserDataController() {
        super();
        chatterService = injector.getInstance(ChatterService.class);
    }

    @FXML
    private void copyAlias() {
        copyToClipboard(chatterService.getAlias());
    }

    @FXML
    private void copyPublicKey() {
        copyToClipboard(chatterService.getPublicKey());
    }

    private void copyToClipboard(String content) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);
    }
}
