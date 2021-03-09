package nl.han.asd.submarine.uicontrollers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import nl.han.asd.submarine.App;
import nl.han.asd.submarine.BootstrapModule;
import nl.han.asd.submarine.exception.ChatterServerException;
import nl.han.asd.submarine.exception.TimeoutRuntimeException;
import nl.han.asd.submarine.models.Chatter;
import nl.han.asd.submarine.service.ChatterService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterController {

    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField aliasField;

    @FXML
    private Text errorField;

    private ChatterService chatterService;

    public RegisterController() {
        Injector injector = Guice.createInjector(new BootstrapModule());
        chatterService = injector.getInstance(ChatterService.class);
    }

    @FXML
    private void registerAction(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Attempt made to register.");

        if (usernameField.getText().isBlank() || passwordField.getText().isBlank() || aliasField.getText().isBlank()) {
            LOGGER.log(Level.WARNING, "Registration failed! Not all fields were filled in.");
            errorField.setVisible(true);
        } else {
            try {
                Chatter chatter = new Chatter(
                        usernameField.getText(),
                        passwordField.getText(),
                        aliasField.getText());
                chatterService.registerChatter(chatter);

                errorField.setVisible(true);
                errorField.setText("Successfully registered at Submarine. You can log in now!");
            } catch (ChatterServerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                errorField.setVisible(true);
                errorField.setText(e.getMessage());
            } catch (TimeoutRuntimeException e) {
                LOGGER.log(Level.SEVERE, "Something went wrong while registering the chatter: "
                        , e);
                errorField.setVisible(true);
                errorField.setText("Something went wrong while registering the chatter.");
            }
        }
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        App.getInstance().replaceSceneContent("loginUI.fxml");
    }

}
