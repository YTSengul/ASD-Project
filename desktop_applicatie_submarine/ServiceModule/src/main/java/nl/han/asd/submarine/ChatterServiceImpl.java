package nl.han.asd.submarine;

import com.google.gson.Gson;
import nl.han.asd.submarine.connection.HandleChatterRequest;
import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.exception.ChatterServerException;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.models.routing.HTTPRequest;
import nl.han.asd.submarine.models.routing.Onion;
import nl.han.asd.submarine.persistence.PersistenceModule;
import nl.han.asd.submarine.routing.RouteModule;
import nl.han.asd.submarine.service.ChatterService;
import nl.han.asd.submarine.util.IpResolver;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.KeyPair;
import java.util.Base64;

public class ChatterServiceImpl implements ChatterService {
    @Inject
    PersistenceModule persistenceModule;

    @Inject
    AsymmetricEncryption asymmetricEncryption;

    @Inject
    RouteModule routeModule;

    @Inject
    HandleChatterRequest handleChatterRequest;

    @Inject
    @Named("clientPort")
    private int clientPort;

    @Inject
    @Named("encryptedSymmetricKey")
    private String encryptedSymmetricKey;

    @Inject
    @Named("chatterServerIp")
    private String serverIp;

    @Override
    public void registerChatter(Chatter chatter) {
        if (chatter == null) {
            throw new IllegalArgumentException("Chatter is null");
        }

        // TODO make a function that gets the ip of the chatter
        chatter.setIpAddress(IpResolver.getHostIp());
        chatter.setPort(clientPort);

        KeyPair keyPair = asymmetricEncryption.generateRandomKeyPair();

        chatter.setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        HTTPRequest<Chatter> httpRequest = new HTTPRequest<>("http://" + serverIp + "/chatter" + "/create", "POST", chatter);

        String dataWithHttp = new Gson().toJson(httpRequest);

        Onion onion = routeModule.makeOnion(dataWithHttp, chatter.getAlias(), 3, encryptedSymmetricKey, true);
        var result = handleChatterRequest.handleChatter(onion);

        if (result != 201) {
            switch (result) {
                case 400:
                    throw new ChatterServerException("Not all fields were filled in.");
                case 409:
                    throw new ChatterServerException("Username or alias already exists.");
                default:
                    throw new ChatterServerException("Something went wrong while registering.");
            }
        }

        UserData userData = new UserData();
        userData.setAlias(chatter.getAlias());
        userData.setUserName(chatter.getUsername());
        userData.setPublicKey(chatter.getPublicKey());
        userData.setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));

        persistenceModule.saveChatter(userData);

    }

    @Override
    public void loginChatter(ChatterLoginDTO chatterLoginDTO) {

        if (chatterLoginDTO == null) {
            throw new IllegalArgumentException("Chatter is null");
        }

        chatterLoginDTO.setIpAddress(IpResolver.getHostIp());

        HTTPRequest<ChatterLoginDTO> httpRequest = new HTTPRequest<>("http://" + serverIp + "/chatter" + "/login", "PUT", chatterLoginDTO);
        String dataWithHttp = new Gson().toJson(httpRequest);

        Onion onion = routeModule.makeOnion(dataWithHttp, chatterLoginDTO.getUsername(), 3, encryptedSymmetricKey, true);
        var result = handleChatterRequest.handleChatter(onion);

        if (result != 200) {
            switch (result) {
                case 400:
                    throw new ChatterServerException("Username or password is incorrect.");
                case 406:
                    throw new ChatterServerException("The given IPv4 address is invalid.");
                default:
                    throw new ChatterServerException("Something went wrong while logging in.");
            }
        }

        persistenceModule.loginChatter(chatterLoginDTO);

    }

    @Override
    public boolean userExists(String username) {
        return username.equals("chatter1") || username.equals("keeshond") || username.equals("hackerman") || username.equals("test");
    }

    @Override
    public String getPublicKey() {
        return persistenceModule.getPublicKey();
    }

    @Override
    public String getAlias() {
        return persistenceModule.getAlias();
    }
}
