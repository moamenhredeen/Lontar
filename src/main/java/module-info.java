module app.orgx.desktop {
    requires javafx.controls;
    requires org.apache.logging.log4j;

    opens app.orgx.desktop to javafx.fxml;
    exports app.orgx.desktop;
}