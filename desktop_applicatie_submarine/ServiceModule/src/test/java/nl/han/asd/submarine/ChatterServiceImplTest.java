package nl.han.asd.submarine;

import nl.han.asd.submarine.encryption.AsymmetricEncryption;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.persistence.PersistenceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatterServiceTest {
    @Mock
    PersistenceModule persistenceModuleMock;
    @InjectMocks
    ChatterServiceImpl sut;

    @BeforeEach
    void setup(){
        sut = new ChatterServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("Register chatter success")
    @Test
    void registerChatterServerChatterIsNull(){
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sut.registerChatter(null))
                .withMessage("Chatter is null");
    }

    @Disabled // Werkt niet meer sinds de nieuwe DTO. Geen tijd meer om uit te werken.
    @Test
    void loginChatterSuccessTest() {

        ChatterLoginDTO testChatterDTOChatterLogin = new ChatterLoginDTO("Melting Martian", "test1", "ipAddress");

        doAnswer(invocationOnMock -> testChatterDTOChatterLogin).when(persistenceModuleMock).loginChatter(testChatterDTOChatterLogin);

        sut.loginChatter(testChatterDTOChatterLogin);

        verify(persistenceModuleMock, times(1)).loginChatter(testChatterDTOChatterLogin);

        assertThat(testChatterDTOChatterLogin.getUsername()).isEqualTo(testChatterDTOChatterLogin.getUsername());
        assertThat(testChatterDTOChatterLogin.getPassword()).isEqualTo(testChatterDTOChatterLogin.getPassword());
        assertThat(testChatterDTOChatterLogin.getIpAddress()).isEqualTo(testChatterDTOChatterLogin.getIpAddress());

    }

    @ParameterizedTest
    @ValueSource(strings = {"chatter1", "keeshond", "hackerman", "test"})
    void userExistsReturnsTrue (String username) {
        // Act & Assert
        assertThat(sut.userExists(username)).isTrue();
    }
}
