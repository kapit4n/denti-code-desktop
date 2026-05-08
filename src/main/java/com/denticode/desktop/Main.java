package com.denticode.desktop;

import atlantafx.base.theme.PrimerLight;
import com.denticode.desktop.core.AppContext;
import com.denticode.desktop.core.Seeder;
import com.denticode.desktop.ui.Router;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX entry point. Wires the {@link AppContext} once, runs the seeder
 * if needed, and hands control off to the {@link Router}.
 */
public final class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private AppContext app;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        this.app = new AppContext();
        if (app.config().demoMode()) {
            try {
                new Seeder(app).seedIfEmpty();
            } catch (RuntimeException e) {
                log.warn("Seeder failed: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void start(Stage stage) {
        Router router = new Router(stage, app);
        router.start();
    }

    @Override
    public void stop() {
        if (app != null) app.close();
        Platform.exit();
    }
}
