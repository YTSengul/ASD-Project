package nl.han.asd.submarine.services;

import nl.han.asd.submarine.models.Chatter;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class ChatterAuthenticationServiceImpl implements ChatterAuthenticationService {
    @Override
    public boolean chatterIsAuthentic(Chatter chatter, String passwordToCheck) {
        try {
            String storedSalt = chatter.getPasswordSalt();
            String storedHash = chatter.getPasswordHash();

            String hashToCompare = saltAndSlowlyHashPassword(passwordToCheck, storedSalt);
            return passwordHashesAreTheSame(storedHash, hashToCompare);
        } catch (NullPointerException e) {
            return false;
        }

    }

    @Override
    public void updatePassword(Chatter chatter, String newPassword) {
        String salt = generateSalt();
        String hashedPassword = saltAndSlowlyHashPassword(newPassword, salt);

        chatter.setPasswordHash(hashedPassword);
        chatter.setPasswordSalt(salt);
    }

    String saltAndSlowlyHashPassword(String plainTextPassword, String salt) {
        return BCrypt.hashpw(plainTextPassword, salt);
    }

    String generateSalt() {
        return BCrypt.gensalt();
    }

    boolean passwordHashesAreTheSame(String passwordHash1, String passwordHash2) {
        if (passwordHash1.isBlank() || passwordHash2.isBlank()) {
            throw new IllegalArgumentException("Cannot compare passwords when either password is empty.");
        }
        return passwordHash1.equals(passwordHash2);
    }
}
