package nl.han.asd.submarine.integrationTests;

import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.repositories.ChatterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-test")
@SpringBootTest(properties = {
        "spring.data.mongodb.database=submarine_test"
})
class ChatterRepositoryIT {

    private final static String ALIAS = "TEST_ALIAS";
    private final static String ALIAS_2 = "TEST_ALIAS_2";
    private final static String PUBLIC_KEY = "TEST_PUBLIC_KEY";
    private final static String USERNAME_1 = "TEST_USERNAME";
    private final static String USERNAME_2 = "TEST_USERNAME_2";
    private final static String PASSWORD = "TEST_PASSWORD";
    private final static String PASSWORD_2 = "TEST_PASSWORD_2";
    private final static String PASSWORD_SALT = "TEST_SALT";
    private final static String IP_ADDRESS = "TEST_IP";
    @Autowired
    ChatterRepository chatterRepository;
    private Chatter chatter;

    @BeforeEach
    void beforeEach() {
        chatterRepository.deleteAll();
        chatter = new Chatter(ALIAS, PUBLIC_KEY, USERNAME_1, PASSWORD, PASSWORD_SALT, IP_ADDRESS);
    }

    @Test
    void checkIfChatterIsSavedCorrectly() {
        // Arrange

        // Act
        chatterRepository.save(chatter);

        // Assert
        List<Chatter> retrievedChatter = chatterRepository.findAll();
        assertThat(retrievedChatter.size()).isEqualTo(1);
        assertThat(retrievedChatter.get(0).getAlias()).isEqualTo(ALIAS);
        assertThat(retrievedChatter.get(0).getUsername()).isEqualTo(USERNAME_1);
        assertThat(retrievedChatter.get(0).getIpAddress()).isEqualTo(IP_ADDRESS);
        assertThat(retrievedChatter.get(0).getPasswordHash()).isEqualTo(PASSWORD);
        assertThat(retrievedChatter.get(0).getPublicKey()).isEqualTo(PUBLIC_KEY);
    }

    @Test
    void checkIfFindIpAddressByAliasReturnsCorrectChatter() {
        // Arrange
        Chatter chatter = new Chatter(ALIAS, PUBLIC_KEY, USERNAME_1, PASSWORD, PASSWORD_SALT, IP_ADDRESS);

        // Act
        chatterRepository.save(chatter);
        var result = chatterRepository.findIpAddressByAlias(ALIAS);

        // Assert
        assertThat(result.getAlias()).isEqualTo(ALIAS);
        assertThat(result.getUsername()).isEqualTo(USERNAME_1);
        assertThat(result.getIpAddress()).isEqualTo(IP_ADDRESS);
        assertThat(result.getPasswordHash()).isEqualTo(PASSWORD);
        assertThat(result.getPublicKey()).isEqualTo(PUBLIC_KEY);
    }

    @Test
    void checkIfFindIpAddressAndPortByAliasReturnsNullIfAliasNotFound() {
        // Arrange
        Chatter chatter = new Chatter(ALIAS, PUBLIC_KEY, USERNAME_1, PASSWORD, PASSWORD_SALT, IP_ADDRESS);

        // Act
        chatterRepository.save(chatter);
        var result = chatterRepository.findIpAddressByAlias("not-the-right-alias");
        // Assert
        assertThat(result).isNull();

    }

    @Test
    void checkIfDuplicateAliasReturnsTrue() {
        // Arrange
        chatterRepository.save(chatter);

        // Act
        boolean result = chatterRepository.existsByAliasOrUsername(ALIAS, USERNAME_2);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void checkIfDuplicateUsernameReturnsTrue() {
        // Arrange
        chatterRepository.save(chatter);

        // Act
        boolean result = chatterRepository.existsByAliasOrUsername(ALIAS_2, USERNAME_1);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void findByUsernameReturnsCorrectUser() {
        // Arrange
        chatterRepository.save(chatter);
        chatterRepository.save(new Chatter(ALIAS_2, PUBLIC_KEY, USERNAME_2, PASSWORD, PASSWORD_SALT, IP_ADDRESS));
        // Act
        Chatter result = chatterRepository.findDistinctByUsername(USERNAME_1);
        // Assert
        assertThat(result.getAlias()).isEqualTo(ALIAS);
        assertThat(result.getUsername()).isEqualTo(USERNAME_1);
        assertThat(result.getIpAddress()).isEqualTo(IP_ADDRESS);
        assertThat(result.getPasswordHash()).isEqualTo(PASSWORD);
        assertThat(result.getPublicKey()).isEqualTo(PUBLIC_KEY);
    }

    @Test
    void findByUsernameReturnsNothingIfUsernameIsWrong() {
        // Arrange
        chatterRepository.save(chatter);
        // Act
        Chatter result = chatterRepository.findDistinctByUsername(USERNAME_2);
        // Assert
        assertThat(result).isNull();
    }

}
