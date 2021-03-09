package nl.han.asd.submarine.uicontrollers.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatUtil {

    protected static String getTimeOfLocalDateTime(LocalDateTime timeStamp) {
        return timeStamp.minusHours(2).format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
