package nl.han.asd.submarine.service;

import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.models.ChatterLoginDTO;

public interface ChatterService {

    void loginChatter(ChatterLoginDTO chatterLoginDTO);

    void registerChatter(Chatter chatter);

    boolean userExists(String username);

    String getPublicKey();

    String getAlias();

}
