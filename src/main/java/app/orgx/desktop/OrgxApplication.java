package app.orgx.desktop;

import app.orgx.desktop.core.AppContext;
import app.orgx.desktop.core.events.NoteOpened;
import app.orgx.desktop.core.events.ThemeChanged;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class OrgxApplication extends Application {

    private static final Logger log = LogManager.getLogger(OrgxApplication.class);
    private AppContext context;

    @Override
    public void start(Stage stage) {
        log.info("Starting application");

        try {
            context = new AppContext();
            context.initUI();
        } catch (IOException e) {
            log.error("Failed to initialize application", e);
            return;
        }

        var shell = context.getAppShell();
        var config = context.getConfigManager().load();

        var scene = new Scene(shell, 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/themes/" + config.theme() + ".css").toExternalForm());

        // Theme switching
        context.getEventBus().subscribe(ThemeChanged.class, e -> {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/themes/" + e.theme() + ".css").toExternalForm());
        });

        // Global keyboard shortcuts
        setupKeyboardShortcuts(scene, stage);

        stage.setScene(scene);
        stage.setTitle("Orgx");
        stage.setOnCloseRequest(e -> {
            try {
                context.shutdown();
            } catch (IOException ignored) {}
        });
        stage.initStyle(StageStyle.EXTENDED);
        stage.show();

        // Open last vault or prompt for one
        if (!config.lastOpenedVault().isBlank()) {
            var vaultEntry = config.vaults().stream()
                    .filter(v -> v.name().equals(config.lastOpenedVault()))
                    .findFirst();
            vaultEntry.ifPresent(v -> {
                try {
                    context.openVault(v.path());
                } catch (IOException ignored) {}
            });
        } else if (config.vaults().isEmpty()) {
            var chooser = new javafx.stage.DirectoryChooser();
            chooser.setTitle("Open Vault");
            var dir = chooser.showDialog(stage);
            if (dir != null) {
                try {
                    context.openVault(dir.toPath());
                } catch (IOException ignored) {}
            }
        }
    }

    private void setupKeyboardShortcuts(Scene scene, Stage stage) {
        scene.setOnKeyPressed(e -> {
            // Ctrl+P — Command palette
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                var palette = context.getCommandPalette();
                if (palette.isShowing()) palette.hide();
                else palette.show();
                e.consume();
            }
            // Ctrl+B — Toggle file tree
            else if (e.isControlDown() && !e.isShiftDown() && e.getCode() == KeyCode.B) {
                context.getAppShell().toggleFileTree();
                e.consume();
            }
            // Ctrl+Shift+B — Toggle backlinks
            else if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.B) {
                context.getAppShell().toggleBacklinks();
                e.consume();
            }
            // Ctrl+S — Save
            else if (e.isControlDown() && e.getCode() == KeyCode.S) {
                try {
                    context.getNoteEditor().save();
                } catch (IOException ignored) {}
                e.consume();
            }
            // Ctrl+W — Close current note
            else if (e.isControlDown() && e.getCode() == KeyCode.W) {
                if (context.checkDirtyState(stage)) {
                    context.getNoteEditor().close();
                }
                e.consume();
            }
            // Ctrl+N — New note
            else if (e.isControlDown() && e.getCode() == KeyCode.N) {
                e.consume();
            }
            // Alt+Left — Navigate back
            else if (e.isAltDown() && e.getCode() == KeyCode.LEFT) {
                var history = context.getNavigationHistory();
                if (history.canGoBack()) {
                    context.setNavigatingHistory(true);
                    var note = history.back();
                    context.getEventBus().publish(new NoteOpened(note));
                    context.setNavigatingHistory(false);
                }
                e.consume();
            }
            // Alt+Right — Navigate forward
            else if (e.isAltDown() && e.getCode() == KeyCode.RIGHT) {
                var history = context.getNavigationHistory();
                if (history.canGoForward()) {
                    context.setNavigatingHistory(true);
                    var note = history.forward();
                    context.getEventBus().publish(new NoteOpened(note));
                    context.setNavigatingHistory(false);
                }
                e.consume();
            }
            // Ctrl+Shift+F — Content search
            else if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.F) {
                var palette = context.getCommandPalette();
                palette.show();
                e.consume();
            }
        });
    }
}
