package nl.han.asd.submarine.models.message.system;

import nl.han.asd.submarine.models.message.Message;

public class SystemMessage extends Message {
    private final SystemCommand command;

    public SystemMessage(SystemCommand command, String conversationId) {
        super("SUBMARINE_SYSTEM", conversationId);
        this.command = command;
    }

    public SystemCommand getCommand() {
        return command;
    }
}
