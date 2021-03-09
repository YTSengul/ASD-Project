package nl.han.asd.submarine.util;

import nl.han.asd.submarine.exceptions.NoSuchMethodRuntimeException;
import nl.han.asd.submarine.models.Chatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

//Suppress warnings that wrongly state that unit test can be private.
// Suppress SonarLint warnings for the getters' return values which are not used in the test
@SuppressWarnings({"ResultOfMethodCallIgnored", "WeakerAccess"})
class FieldCheckerTest {

    Chatter chatterMock;

    @BeforeEach
    void setup() {
        chatterMock = mock(Chatter.class);
        when(chatterMock.getPublicKey()).thenReturn("TEST");
        when(chatterMock.getUsername()).thenReturn("TEST");
    }

    @Test
    void allFieldsOfClassAreSet() {
        // Arrange

        // Act & Assert
        assertThat(FieldChecker.areFieldsNotBlankOrNull(new String[]{"publicKey", "username"}, chatterMock)).isTrue();
        verify(chatterMock).getPublicKey();
        verify(chatterMock).getUsername();
    }

    @Test
    void returnsFalseIfPasswordFieldIsEmpty() {
        // Arrange

        // Act & Assert
        assertThat(FieldChecker.areFieldsNotBlankOrNull(new String[]{"publicKey", "username", "passwordHash"}, chatterMock)).isFalse();
        verify(chatterMock).getPublicKey();
        verify(chatterMock).getUsername();
        verify(chatterMock).getPasswordHash();
    }

    @Test
    void testIfOneFieldIsEmptyThatNotAllFieldsGetChecked() {
        // Arrange

        // Act & Assert
        assertThat(FieldChecker.areFieldsNotBlankOrNull(new String[]{"passwordHash", "publicKey", "username"}, chatterMock)).isFalse();
        verify(chatterMock).getPasswordHash();
        verify(chatterMock, never()).getPublicKey();
        verify(chatterMock, never()).getUsername();
    }

    @Test
    void returnsExceptionIfAGetterIsNotDefined() {
        // Arrange

        // Act & Assert
        assertThatExceptionOfType(NoSuchMethodRuntimeException.class)
                .isThrownBy(() -> FieldChecker.areFieldsNotBlankOrNull(new String[]{"notExistingMethod"}, chatterMock))
                .withMessage("Something went wrong. A getter method was not found. Contact the system admin");

    }
}
