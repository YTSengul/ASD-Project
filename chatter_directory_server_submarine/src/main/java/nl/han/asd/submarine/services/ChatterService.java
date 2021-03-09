package nl.han.asd.submarine.services;

import nl.han.asd.submarine.controllers.dto.ChatterLoginDTO;
import nl.han.asd.submarine.controllers.dto.ChatterRegistrationDTO;

public interface ChatterService {
    void validateAndCreateChatter(ChatterRegistrationDTO chatterRegistrationDTO);

    void loginChatter(ChatterLoginDTO chatterLoginDTO);

    String getChatterIpByAlias(String alias);
}
