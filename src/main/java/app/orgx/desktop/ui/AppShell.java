package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class AppShell extends BorderPane {

    private final StackPane centerStack;
    private final StatusBar statusBar;
    private final HeaderBar headerBar;

    private Pane fileTreePanel = new Pane();
    private Pane editorPanel = new Pane();
    private Pane backlinksPanel = new Pane();

    private boolean fileTreeVisible = false;
    private boolean backlinksVisible = false;

    public AppShell(EventBus eventBus) {
        headerBar = new HeaderBar();
        setTop(headerBar);

        centerStack = new StackPane(editorPanel);
        setCenter(centerStack);

        statusBar = new StatusBar();
        // Hidden by default per spec
    }

    public void setFileTreePanel(Pane panel) {
        this.fileTreePanel = panel;
        if (fileTreeVisible) {
            setLeft(panel);
        }
    }

    public void setEditorPanel(Pane panel) {
        this.editorPanel = panel;
        // Replace the first child (editor placeholder) but preserve overlay children (command palette)
        if (!centerStack.getChildren().isEmpty()) {
            centerStack.getChildren().set(0, panel);
        } else {
            centerStack.getChildren().add(panel);
        }
    }

    public void setBacklinksPanel(Pane panel) {
        this.backlinksPanel = panel;
        if (backlinksVisible) {
            setRight(panel);
        }
    }

    public StackPane getCenterStack() {
        return centerStack;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setStatusBarVisible(boolean visible) {
        setBottom(visible ? statusBar : null);
    }

    public void toggleFileTree() {
        fileTreeVisible = !fileTreeVisible;
        setLeft(fileTreeVisible ? fileTreePanel : null);
    }

    public void toggleBacklinks() {
        backlinksVisible = !backlinksVisible;
        setRight(backlinksVisible ? backlinksPanel : null);
    }

    public boolean isFileTreeVisible() { return fileTreeVisible; }
    public boolean isBacklinksVisible() { return backlinksVisible; }
}
