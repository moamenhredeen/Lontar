package app.orgx.desktop.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class StatusBar extends HBox {

    private final Label pathLabel = new Label();
    private final Label wordCountLabel = new Label();
    private final Label cursorLabel = new Label();

    public StatusBar() {
        getStyleClass().add("status-bar");
        setPadding(new Insets(2, 8, 2, 8));
        setSpacing(16);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(pathLabel, spacer, wordCountLabel, cursorLabel);
    }

    public void setPath(String path) {
        pathLabel.setText(path);
    }

    public void setWordCount(int count) {
        wordCountLabel.setText("Words: " + count);
    }

    public void setCursorPosition(int line, int col) {
        cursorLabel.setText("Ln " + line + ", Col " + col);
    }
}
