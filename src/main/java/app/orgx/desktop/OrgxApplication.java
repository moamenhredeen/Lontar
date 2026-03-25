package app.orgx.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrgxApplication extends Application {

    public static final Logger log = LogManager.getLogger(OrgxApplication.class);

    @Override
    public void start(Stage stage)  {
        log.info("Starting application");

        var headerBar = new HeaderBar();
        var root = new BorderPane();
        root.setTop(headerBar);

        stage.setScene(new Scene(root, 600, 400));
        stage.initStyle(StageStyle.EXTENDED);
        stage.show();
    }
}
