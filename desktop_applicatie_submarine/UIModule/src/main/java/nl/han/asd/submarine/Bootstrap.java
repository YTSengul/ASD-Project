package nl.han.asd.submarine;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;

// if name is edited than please also edit in maven assemble plugin in assemble module
public class Bootstrap {

    private static Injector injector;

    public static void main(String[] args) {
        injector = Guice.createInjector(new BootstrapModule());
        if (args.length != 2 || args[1] == null || !args[1].equals("headless")) Application.launch(App.class, args);
    }

    public static Injector getInjector() {
        return injector;
    }

}
