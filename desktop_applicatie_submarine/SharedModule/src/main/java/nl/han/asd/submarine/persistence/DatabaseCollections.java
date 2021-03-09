package nl.han.asd.submarine.persistence;

public enum DatabaseCollections {

    CONTACT(Constants.CONTACT),
    CONVERSATION(Constants.CONVERSATION),
    USER_DATA(Constants.USER_DATA);

    private final String value;


    DatabaseCollections(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    // Sad but true, annotations(@Named) don't allow for Enums, only actual constants
    public static class Constants {
        public static final String CONTACT = "contact";
        public static final String CONVERSATION = "conversation";
        public static final String USER_DATA = "userData";
    }
}


