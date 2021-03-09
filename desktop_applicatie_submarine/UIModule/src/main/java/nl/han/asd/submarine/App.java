package nl.han.asd.submarine;

import com.google.inject.Injector;
import com.mongodb.lang.NonNull;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static Stage stage;
    private static App instance;


    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    private static void setStage(Stage stage) {
        if (App.stage == null) App.stage = stage;
    }

    @Override
    public void start(Stage primaryStage) {
        Injector injector = Bootstrap.getInjector();
        MessageService messageService = injector.getInstance(MessageService.class);
        messageService.initializeConnectionModule(25010);

        setStage(primaryStage);
        replaceSceneContent("loginUI.fxml");
        stage.show();
    }

    public void replaceSceneContent(@NonNull String fxml) {
        try {
            LOGGER.log(Level.INFO, () -> "Loading the scene: " + fxml);
            Parent page = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(fxml)));

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(page);
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(page);
            }
            stage.setResizable(false);
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images/logo.png"))));
            stage.setTitle("Submarine Confidential Messaging Service");
            stage.sizeToScene();
            LOGGER.log(Level.INFO, () -> "Successfully loaded the scene: " + fxml);
        } catch (Exception e) {
            throw new IllegalArgumentException("An error occurred while loading the scene: " + fxml, e);
        }
    }
}
