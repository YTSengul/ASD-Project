package nl.han.asd.submarine.persistence;

import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.models.UserData;

public interface PersistUserData {

    String getAlias();

    String getPublicKey();

    String getPrivateKeyOfUser();

    UserData getUserData();

    void loginChatter(ChatterLoginDTO chatterLoginDTO);

    void saveChatter(UserData userData);
}
