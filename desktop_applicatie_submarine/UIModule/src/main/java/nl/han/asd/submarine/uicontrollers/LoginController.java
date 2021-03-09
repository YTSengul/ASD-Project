package nl.han.asd.submarine.uicontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import nl.han.asd.submarine.App;
import nl.han.asd.submarine.exception.ChatterServerException;
import nl.han.asd.submarine.exception.TimeoutRuntimeException;
import nl.han.asd.submarine.models.ChatterLoginDTO;
import nl.han.asd.submarine.service.ChatterService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController extends ControllerBase {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private final ChatterService chatterService;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Text errorField;

    public LoginController() {
        super();
        chatterService = injector.getInstance(ChatterService.class);
    }

    @FXML
    private void loginAction(ActionEvent event) {
        LOGGER.log(Level.INFO, "Attempt made to log in.");

        if (usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
            LOGGER.log(Level.WARNING, "Log in failed! Not all fields were filled in.");
            errorField.setVisible(true);
        } else {
            errorField.setVisible(false);

            if (!usernameField.getText().isBlank() || !passwordField.getText().isBlank()) {
                try {
                    ChatterLoginDTO chatterLoginDTO = new ChatterLoginDTO();
                    chatterLoginDTO.setUsername(usernameField.getText());
                    chatterLoginDTO.setPassword(passwordField.getText());

                    chatterService.loginChatter(chatterLoginDTO);

                    LOGGER.log(Level.INFO, "Logged in successfully.");
                    App.getInstance().replaceSceneContent("chatUI.fxml");
                } catch (ChatterServerException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    errorField.setVisible(true);
                    errorField.setText(e.getMessage());
                } catch (TimeoutRuntimeException e) {
                    LOGGER.log(Level.SEVERE, "Something went wrong while logging in the chatter. "
                            , e);
                    errorField.setVisible(false);
                    errorField.setText("Something went wrong while logging in the chatter.");
                }
            } else {
                LOGGER.log(Level.WARNING, "Log in failed! Invalid log in credentials.");
                errorField.setVisible(true);
                errorField.setText("The log in credentials are invalid.");
            }
        }
    }

    @FXML
    private void registerAction(ActionEvent event) {
        app.replaceSceneContent("registerUI.fxml");
    }

}
