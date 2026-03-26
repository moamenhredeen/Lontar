package app.orgx.desktop;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrgxApplication extends Application {

    private static final Logger log = LogManager.getLogger(OrgxApplication.class);

    @Override
    public void start(Stage stage) {
        log.info("Starting application");

        var eventBus = new EventBus();
        var shell = new AppShell(eventBus);

        var scene = new Scene(shell, 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/themes/light.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Orgx");
        stage.show();
    }
}
