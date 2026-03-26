package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class AppShell extends BorderPane {

    private final SplitPane splitPane;
    private final StackPane centerStack;
    private final StatusBar statusBar;

    // Placeholders — replaced by actual panels in later tasks
    private Pane fileTreePanel = new Pane();
    private Pane editorPanel = new Pane();
    private Pane backlinksPanel = new Pane();

    private boolean fileTreeVisible = true;
    private boolean backlinksVisible = true;

    public AppShell(EventBus eventBus) {
        splitPane = new SplitPane(fileTreePanel, editorPanel, backlinksPanel);
        splitPane.setDividerPositions(0.2, 0.75);

        centerStack = new StackPane(splitPane);
        setCenter(centerStack);

        statusBar = new StatusBar();
        // Hidden by default per spec
    }

    public void setFileTreePanel(Pane panel) {
        var idx = splitPane.getItems().indexOf(fileTreePanel);
        if (idx >= 0) splitPane.getItems().set(idx, panel);
        this.fileTreePanel = panel;
    }

    public void setEditorPanel(Pane panel) {
        var idx = splitPane.getItems().indexOf(editorPanel);
        if (idx >= 0) splitPane.getItems().set(idx, panel);
        this.editorPanel = panel;
    }

    public void setBacklinksPanel(Pane panel) {
        var idx = splitPane.getItems().indexOf(backlinksPanel);
        if (idx >= 0) splitPane.getItems().set(idx, panel);
        this.backlinksPanel = panel;
    }

    public StackPane getCenterStack() {
        return centerStack;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setStatusBarVisible(boolean visible) {
        if (visible) {
            setBottom(statusBar);
        } else {
            setBottom(null);
        }
    }

    public void toggleFileTree() {
        fileTreeVisible = !fileTreeVisible;
        if (fileTreeVisible) {
            if (!splitPane.getItems().contains(fileTreePanel)) {
                splitPane.getItems().addFirst(fileTreePanel);
            }
        } else {
            splitPane.getItems().remove(fileTreePanel);
        }
    }

    public void toggleBacklinks() {
        backlinksVisible = !backlinksVisible;
        if (backlinksVisible) {
            if (!splitPane.getItems().contains(backlinksPanel)) {
                splitPane.getItems().addLast(backlinksPanel);
            }
        } else {
            splitPane.getItems().remove(backlinksPanel);
        }
    }

    public boolean isFileTreeVisible() { return fileTreeVisible; }
    public boolean isBacklinksVisible() { return backlinksVisible; }
}
