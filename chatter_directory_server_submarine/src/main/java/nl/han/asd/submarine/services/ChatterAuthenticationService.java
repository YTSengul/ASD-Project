package nl.han.asd.submarine.services;

import nl.han.asd.submarine.models.Chatter;

public interface ChatterAuthenticationService {
    boolean chatterIsAuthentic(Chatter chatter, String plainTextPassword);

    void updatePassword(Chatter chatter, String plainTextPassword);
}
