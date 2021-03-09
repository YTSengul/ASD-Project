package nl.han.asd.submarine.uicontrollers.chat;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class SpeechBox extends MessageBox {
    private static final Color DEFAULT_SENDER_COLOR = Color.web("#1985a1");
    private static final Color DEFAULT_RECEIVER_COLOR = Color.web("#c5c3c6");
    private static final Color DEFAULT_SYSTEM_COLOR = Color.web("#8CDAF7");

    private final String message;
    private final SpeechDirection direction;

    public SpeechBox(String message, SpeechDirection direction) {
        super(DEFAULT_SENDER_COLOR, DEFAULT_RECEIVER_COLOR, DEFAULT_SYSTEM_COLOR);
        this.message = message;
        this.direction = direction;
        setupElements();
    }

    @Override
    protected void setupElements() {
        displayedText = new Label(message);
        displayedText.setPadding(new Insets(5));
        displayedText.setWrapText(true);
        directionIndicator = new SVGPath();

        switch (direction) {
            case LEFT:
                configureForReceiver();
                break;
            case CENTER:
                configureForSystem();
                break;
            case RIGHT:
            default:
                configureForSender();
                break;
        }
    }
}
