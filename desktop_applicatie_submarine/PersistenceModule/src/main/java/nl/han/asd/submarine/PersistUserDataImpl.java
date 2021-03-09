package nl.han.asd.submarine;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import nl.han.asd.submarine.exceptions.UserDoesNotExistException;
import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.models.UserData;
import nl.han.asd.submarine.persistence.DatabaseCollections;
import nl.han.asd.submarine.persistence.PersistUserData;
import org.bson.Document;

import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;


public class PersistUserDataImpl implements PersistUserData {

    private static final Logger LOG =
            Logger.getLogger(PersistUserDataImpl.class.getName());

    // COLLECTION FIELDS
    private static final String ALIAS = "alias";
    private static final String LAST_LOGIN = "lastLogin";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String USERNAME = "username";
    @Inject
    @Named(DatabaseCollections.Constants.USER_DATA)
    private MongoCollection<Document> collection;

    public String getPublicKey() {
        try {
            Document result = collection.find(exists(ALIAS))
                    .sort(new Document(LAST_LOGIN, -1))
                    .first();
            return result.getString(PUBLIC_KEY);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "Could not find Alias");
            throw e;
        }
    }

    public String getAlias() {
        try {
            Document result = collection.find(exists(ALIAS))
                    .sort(new Document(LAST_LOGIN, -1))
                    .first();
            return result.getString(ALIAS);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "Could not find Alias");
            throw e;
        }
    }

    @Override
    public String getPrivateKeyOfUser() {
        try {
            Document result = collection.find().sort(new Document(LAST_LOGIN, -1)).first();
            return result.getString("privateKey");
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "Could not find private key");
            throw e;
        }
    }

    @Override
    public UserData getUserData() {
        Document result = collection.find().sort(new Document(LAST_LOGIN, -1)).first();
        try {
            UserData userData = new UserData(
                    result.getString("alias"),
                    result.getString("publicKey"),
                    result.getString("privateKey"));
            return userData;
        } catch(NullPointerException e) {
            LOG.log(Level.SEVERE, "The userData in the database seems to be incomplete");
            throw(e);
        }
    }

    @Override
    public void saveChatter(UserData userData) {
        try {
            Document newChatter = new Document();
            newChatter.put(ALIAS, userData.getAlias());
            newChatter.put("publicKey", userData.getPublicKey());
            newChatter.put("privateKey", userData.getPrivateKey());
            newChatter.put("username", userData.getUserName());
            newChatter.put(LAST_LOGIN, new Timestamp(System.currentTimeMillis()));
            collection.insertOne(newChatter);
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loginChatter(ChatterLoginDTO chatterLoginDTO) {
        if (!aliasDoesExistInDatabase(chatterLoginDTO.getUsername())) {
            throw new UserDoesNotExistException("This user could not be found in the database.");
        }

        Document loginDocument = new Document();
        loginDocument.put(USERNAME, chatterLoginDTO.getUsername());

        collection.updateOne(loginDocument, new Document("$set",
                new Document(LAST_LOGIN,
                        new Timestamp(System.currentTimeMillis()))));
    }

    private boolean aliasDoesExistInDatabase(String username) {
        try {
            var result = collection.find(eq(USERNAME, username)).first();
            return result != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

}
