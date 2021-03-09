package nl.han.asd.submarine.services;

import nl.han.asd.submarine.models.Chatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ChatterAuthenticationServiceImplTest {

    private ChatterAuthenticationServiceImpl sut;

    private Chatter chatter1;
    private Chatter chatter2;
    private String password1;
    private String password2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        sut = new ChatterAuthenticationServiceImpl();

        chatter1 = new Chatter();
        chatter1.setUsername("TEST_USERNAME_1");
        chatter1.setPasswordHash("$2a$10$vPZ3EKgL8zt4M2TrlcptA.CYzDD9DzGlgk1U8y9Dh/sgWd3Kh8UmC");
        chatter1.setPasswordSalt("$2a$10$vPZ3EKgL8zt4M2TrlcptA.");

        chatter2 = new Chatter();
        chatter2.setUsername("TEST_USERNAME_2");
        chatter2.setPasswordHash("$2a$10$4.7PQgkJczLNl5TqSqRxs.YE0/cbqsrna0/4j/OFh9hhiq.wR.Yaa");
        chatter2.setPasswordSalt("$2a$10$4.7PQgkJczLNl5TqSqRxs.");

        password1 = "TEST_PASSWORD_1";
        password2 = "TEST_PASSWORD_2";
    }

    @Test
    @DisplayName("Salt generation is random")
    void saltGenerationIsRandom() {
        //Act
        String salt1 = sut.generateSalt();
        String salt2 = sut.generateSalt();
        //Assert
        assertThat(salt1).isNotEqualTo(salt2);
        assertThat(salt1).isNotEmpty();
        assertThat(salt2).isNotEmpty();
    }

    @Test
    @DisplayName("Generated salts are long enough to be unique for each user")
    void generatedSaltsAreLongEnough() {
        //Act
        String salt = sut.generateSalt();
        //Assert
        assertThat(salt.length()).isGreaterThan(16);
    }

    @Test
    @DisplayName("Hashing is not random")
    void hashingIsNotRandom() {
        //Arrange
        String salt = sut.generateSalt();
        String password = "TEST_PASSWORD";
        //Act
        String hash1 = sut.saltAndSlowlyHashPassword(password, salt);
        String hash2 = sut.saltAndSlowlyHashPassword(password, salt);
        //Assert
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).isNotEmpty();
        assertThat(hash2).isNotEmpty();
    }

    @Test
    @DisplayName("Hashing changes the password")
    void hashingChangesThePassword() {
        //Arrange
        String salt = sut.generateSalt();
        String password = "TEST_PASSWORD";
        //Act
        String hash = sut.saltAndSlowlyHashPassword(password, salt);
        //Assert
        assertThat(hash).isNotEqualTo(password);
        assertThat(hash).isNotEmpty();
    }

    @ParameterizedTest
    @CsvSource({"'',''", "'',NOT_EMPTY_HASH", "NOT_EMPTY_HASH,''", "' ',NOT_BLANK_HASH", "NOT_BLANK_HASH, ' '"})
    @DisplayName("Empty passwords will not be compared")
    void emptyPasswordsWontBeCompared(String passwordHash1, String passwordHash2) {
        //Act
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.passwordHashesAreTheSame(passwordHash1, passwordHash2))
                .withMessage("Cannot compare passwords when either password is empty.");
    }

    @Test
    @DisplayName("chatterIsAuthentic returns true with correct password")
    void chatterIsAuthenticReturnsTrueWithCorrectPassword() {
        //Act
        boolean isChatterAuthentic1 = sut.chatterIsAuthentic(chatter1, password1);
        boolean isChatterAuthentic2 = sut.chatterIsAuthentic(chatter2, password2);
        //Assert
        assertThat(isChatterAuthentic1).isTrue();
        assertThat(isChatterAuthentic2).isTrue();
    }

    @Test
    @DisplayName("chatterIsAuthentic returns false with incorrect password")
    void chatterIsAuthenticReturnsFalseWithIncorrectPassword() {
        //Act
        boolean isChatterAuthentic1 = sut.chatterIsAuthentic(chatter1, password2);
        boolean isChatterAuthentic2 = sut.chatterIsAuthentic(chatter2, password1);
        //Assert
        assertThat(isChatterAuthentic1).isFalse();
        assertThat(isChatterAuthentic2).isFalse();
    }


    @Test
    @DisplayName("updatePassword updates both a chatter's passwordHash and passwordSalt")
    void updatePasswordUpdatesBothHashAndSalt() {
        //Arrange
        String oldSalt = chatter1.getPasswordSalt();
        String oldHash = chatter1.getPasswordHash();
        //Act
        sut.updatePassword(chatter1, password2);
        //Assert
        assertThat(chatter1.getPasswordSalt()).isNotEqualTo(oldSalt);
        assertThat(chatter1.getPasswordHash()).isNotEqualTo(oldHash);
    }

    @ParameterizedTest
    @CsvSource({",", "$2a$10$4.7PQgkJczLNl5TqSqRxs.YE0/cbqsrna0/4j/OFh9hhiq.wR.Yaa,", ",$2a$10$4.7PQgkJczLNl5TqSqRxs."})
    @DisplayName("chatterIsAuthentic returns false if nullPointer is thrown if there is no password hash or salt")
    void updatePasswordUpdatesBothHashAndSalt(String passwordHash, String passwordSalt) {
        //Arrange
        chatter1.setPasswordHash(passwordHash);
        chatter1.setPasswordSalt(passwordSalt);
        String oldHash = chatter1.getPasswordHash();
        //Act
        boolean isChatterAuthentic = sut.chatterIsAuthentic(chatter1, password2);
        //Assert
        assertThat(isChatterAuthentic).isFalse();
    }

}
