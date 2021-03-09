package nl.han.asd.submarine.routing;

import nl.han.asd.submarine.models.routing.Onion;

public interface RouteModule {
    Onion makeOnion(String content, String receiverAlias, int lengthOfPath, String encryptedSymmetricKey, boolean isHttpRequest);
}
