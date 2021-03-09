package nl.han.asd.submarine.uicontrollers;

import com.google.inject.Injector;
import nl.han.asd.submarine.App;
import nl.han.asd.submarine.Bootstrap;

public abstract class ControllerBase {
    protected static Injector injector = Bootstrap.getInjector();
    protected App app;

    public ControllerBase() {
        app = injector.getInstance(App.class);
    }
}
