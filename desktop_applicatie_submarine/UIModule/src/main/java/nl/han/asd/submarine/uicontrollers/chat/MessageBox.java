package nl.han.asd.submarine.uicontrollers.chat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public abstract class MessageBox extends HBox {
    private final Color DEFAULT_SENDER_COLOR;
    private final Color DEFAULT_RECEIVER_COLOR;
    private final Color DEFAULT_SYSTEM_COLOR;
    protected Label displayedText;
    protected SVGPath directionIndicator;
    private Background defaultSenderBackground;
    private Background defaultReceiverBackground;
    private Background defaultSystemBackground;

    public MessageBox(Color defaultSenderColor, Color defaultReceiverColor, Color defaultSystemColor) {
        DEFAULT_SENDER_COLOR = defaultSenderColor;
        DEFAULT_RECEIVER_COLOR = defaultReceiverColor;
        DEFAULT_SYSTEM_COLOR = defaultSystemColor;
        initialiseDefaults();
    }

    protected void initialiseDefaults() {
        defaultSenderBackground = new Background(new BackgroundFill(DEFAULT_SENDER_COLOR,
                new CornerRadii(5, 0, 5, 5, false), Insets.EMPTY));
        defaultReceiverBackground = new Background(new BackgroundFill(DEFAULT_RECEIVER_COLOR,
                new CornerRadii(0, 5, 5, 5, false), Insets.EMPTY));
        defaultSystemBackground = new Background(new BackgroundFill(DEFAULT_SYSTEM_COLOR,
                new CornerRadii(5, 5, 5, 5, false), Insets.EMPTY));
    }

    protected void configureForSender() {
        displayedText.setBackground(defaultSenderBackground);
        displayedText.setAlignment(Pos.CENTER_RIGHT);
        displayedText.setTextFill(Color.web("#dcdcdd"));
        directionIndicator.setContent("M10 0 L0 10 L0 0 Z");
        directionIndicator.setFill(DEFAULT_SENDER_COLOR);
        HBox container = new HBox(displayedText, directionIndicator);

        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_RIGHT);
    }

    protected void configureForReceiver() {
        displayedText.setBackground(defaultReceiverBackground);
        displayedText.setAlignment(Pos.CENTER_LEFT);
        displayedText.setTextFill(Color.web("#46494c"));
        directionIndicator.setContent("M0 0 L10 0 L10 10 Z");
        directionIndicator.setFill(DEFAULT_RECEIVER_COLOR);

        HBox container = new HBox(directionIndicator, displayedText);
        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_LEFT);
    }

    protected void configureForSystem() {
        displayedText.setBackground(defaultSystemBackground);
        displayedText.setAlignment(Pos.CENTER_LEFT);
        displayedText.setTextFill(Color.web("#474954"));
        displayedText.setStyle("-fx-font-weight: bold;");
        directionIndicator.setFill(DEFAULT_RECEIVER_COLOR);

        HBox container = new HBox(directionIndicator, displayedText);
        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER);
    }

    protected abstract void setupElements();
}
