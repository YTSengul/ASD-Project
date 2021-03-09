package nl.han.asd.submarine.uicontrollers.chat;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.time.LocalDateTime;

import static nl.han.asd.submarine.uicontrollers.chat.ChatUtil.getTimeOfLocalDateTime;

public class FileBox extends MessageBox {
    private static final Color DEFAULT_SENDER_COLOR = Color.web("#1985a1");
    private static final Color DEFAULT_RECEIVER_COLOR = Color.web("#c5c3c6");
    private static final Color DEFAULT_SYSTEM_COLOR = Color.web("#8CDAF7");
    private static final String[] FILE_UNITS = {"B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    private final SpeechDirection direction;
    private final String fileName;
    private final long sizeInBytes;
    private final String sender;
    private final LocalDateTime timeSend;


    public FileBox(String fileName, long sizeInBytes, LocalDateTime localDateTime, SpeechDirection direction) {
        super(DEFAULT_SENDER_COLOR, DEFAULT_RECEIVER_COLOR, DEFAULT_SYSTEM_COLOR);
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.direction = direction;
        this.sender = null;
        this.timeSend = localDateTime;
        setupElements();
        initialiseDefaults();
    }

    public FileBox(String fileName, String sender, long sizeInBytes, LocalDateTime localDateTime, SpeechDirection direction) {
        super(DEFAULT_SENDER_COLOR, DEFAULT_RECEIVER_COLOR, DEFAULT_SYSTEM_COLOR);
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.direction = direction;
        this.sender = sender;
        this.timeSend = localDateTime;
        initialiseDefaults();
        setupElements();
    }

    private static String calculateFileSize(long fileSize) {
        int i;
        for (i = 0; i < FILE_UNITS.length; i++) {
            if (fileSize < 1024) {
                break;
            }
            fileSize = fileSize / 1024;
        }
        return fileSize + " " + FILE_UNITS[i];
    }

    @Override
    protected void setupElements() {
        String displayText = "";
        if (sender != null) {
            displayText = sender + " ";
        }
        displayText = displayText +
                "(" + getTimeOfLocalDateTime(timeSend) + ")" + "\n" +
                fileName + ", " + calculateFileSize(sizeInBytes);


        displayedText = new Label(displayText);
        displayedText.setPadding(new Insets(5));
        displayedText.setWrapText(true);
        directionIndicator = new SVGPath();

        if (direction == SpeechDirection.LEFT) {
            configureForReceiver();
        } else {
            configureForSender();
        }
    }
}
