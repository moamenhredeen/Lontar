package app.orgx.desktop.core;

import app.orgx.desktop.config.ConfigManager;
import app.orgx.desktop.editor.NoteEditor;
import app.orgx.desktop.editor.NoteEditorPanel;
import app.orgx.desktop.search.SearchEngine;
import app.orgx.desktop.ui.*;
import app.orgx.desktop.ui.palette.*;
import app.orgx.desktop.vault.*;
import app.orgx.desktop.core.events.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AppContext {

    private final EventBus eventBus;
    private final ConfigManager configManager;
    private final VaultManager vaultManager;
    private final LinkIndex linkIndex;
    private final SearchEngine searchEngine;
    private final NavigationHistory navigationHistory;
    private final NoteEditor noteEditor;
    private FileWatcher fileWatcher;

    // UI components
    private AppShell appShell;
    private FileTreePanel fileTreePanel;
    private NoteEditorPanel noteEditorPanel;
    private BacklinksPanel backlinksPanel;
    private CommandPalette commandPalette;

    // Flag to prevent navigation history infinite loop
    private boolean navigatingHistory = false;

    public AppContext() throws IOException {
        this.eventBus = new EventBus();
        this.configManager = ConfigManager.createDefault();
        this.vaultManager = new VaultManager(eventBus);
        this.linkIndex = new LinkIndex();
        this.searchEngine = new SearchEngine();
        this.navigationHistory = new NavigationHistory();
        this.noteEditor = new NoteEditor(eventBus, vaultManager);

        wireEvents();
    }

    public void initUI() {
        appShell = new AppShell(eventBus);
        fileTreePanel = new FileTreePanel(eventBus, vaultManager);
        noteEditorPanel = new NoteEditorPanel(eventBus, noteEditor);
        backlinksPanel = new BacklinksPanel(eventBus, linkIndex);

        appShell.setFileTreePanel(fileTreePanel);
        appShell.setEditorPanel(noteEditorPanel);
        appShell.setBacklinksPanel(backlinksPanel);

        // Setup command palette
        var commandProvider = new CommandProvider();
        registerCommands(commandProvider);

        commandPalette = new CommandPalette(eventBus, List.of(
                new FileSearchProvider(searchEngine, this::openNoteByTitle),
                new ContentSearchProvider(searchEngine, this::openNoteByTitle),
                commandProvider,
                new VaultSwitchProvider(
                        () -> configManager.load().vaults(),
                        this::switchVault)
        ));
        appShell.getCenterStack().getChildren().add(commandPalette);

        // Apply config
        var config = configManager.load();
        appShell.setStatusBarVisible(config.showStatusBar());
    }

    public AppShell getAppShell() { return appShell; }
    public EventBus getEventBus() { return eventBus; }
    public CommandPalette getCommandPalette() { return commandPalette; }
    public ConfigManager getConfigManager() { return configManager; }
    public NoteEditor getNoteEditor() { return noteEditor; }
    public NavigationHistory getNavigationHistory() { return navigationHistory; }
    public void setNavigatingHistory(boolean v) { this.navigatingHistory = v; }

    public void openVault(Path path) throws IOException {
        if (fileWatcher != null) fileWatcher.stop();
        vaultManager.openVault(path);
        linkIndex.rebuild(vaultManager.getVault());
        searchEngine.rebuildIndex(vaultManager.getVault());
        fileTreePanel.loadVault(vaultManager.getVault());

        fileWatcher = new FileWatcher(path, eventBus);
        fileWatcher.start();
    }

    private void wireEvents() {
        // When a note is opened (not via back/forward), push to navigation history
        eventBus.subscribe(NoteOpened.class, e -> {
            if (!navigatingHistory) {
                navigationHistory.push(e.note());
            }
        });

        // When a link is clicked, resolve and open
        eventBus.subscribe(LinkClicked.class, e -> {
            var vault = vaultManager.getVault();
            if (vault == null) return;
            var note = vault.getNote(e.target());
            if (note != null) {
                eventBus.publish(new NoteOpened(note));
            } else {
                // Create new note for non-existent link
                try {
                    var newNote = vaultManager.createNote(e.target());
                    eventBus.publish(new NoteOpened(newNote));
                } catch (IOException ignored) {}
            }
        });

        // When a note is saved, update link index and search
        eventBus.subscribe(NoteSaved.class, e -> {
            linkIndex.updateNote(e.note());
            try {
                searchEngine.updateNote(e.note());
            } catch (IOException ignored) {}
        });

        // External file changes
        eventBus.subscribe(NoteExternallyChanged.class, e -> {
            try {
                vaultManager.loadNote(e.path());
            } catch (IOException ignored) {}
        });
    }

    private void registerCommands(CommandProvider provider) {
        provider.register("New Note", "Create a new note", () -> {});
        provider.register("Toggle File Tree", "Show/hide file tree sidebar",
                () -> appShell.toggleFileTree());
        provider.register("Toggle Backlinks", "Show/hide backlinks panel",
                () -> appShell.toggleBacklinks());
        provider.register("Toggle Status Bar", "Show/hide status bar", () -> {
            var config = configManager.load();
            config.setShowStatusBar(!config.showStatusBar());
            configManager.save(config);
            appShell.setStatusBarVisible(config.showStatusBar());
        });
        provider.register("Switch Theme", "Toggle light/dark theme", () -> {
            var config = configManager.load();
            var newTheme = "light".equals(config.theme()) ? "dark" : "light";
            config.setTheme(newTheme);
            configManager.save(config);
            eventBus.publish(new ThemeChanged(newTheme));
        });
        provider.register("Delete Note", "Delete the current note", () -> {
            var note = noteEditor.getCurrentNote();
            if (note != null) {
                try {
                    vaultManager.deleteNote(note.path());
                    noteEditor.close();
                } catch (IOException ignored) {}
            }
        });
        provider.register("Rename Note", "Rename the current note", () -> {});
        provider.register("Open Vault Folder", "Open vault in file manager", () -> {
            var vault = vaultManager.getVault();
            if (vault != null) {
                try {
                    java.awt.Desktop.getDesktop().open(vault.rootPath().toFile());
                } catch (Exception ignored) {}
            }
        });
        provider.register("Reload Vault", "Re-scan vault files", () -> {
            var vault = vaultManager.getVault();
            if (vault != null) {
                try {
                    openVault(vault.rootPath());
                } catch (IOException ignored) {}
            }
        });
    }

    public boolean checkDirtyState(javafx.stage.Window owner) {
        if (!noteEditor.isModified()) return true;
        var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Save changes to " + noteEditor.getCurrentNote().title() + "?");
        alert.getButtonTypes().setAll(
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO,
                javafx.scene.control.ButtonType.CANCEL);
        var result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == javafx.scene.control.ButtonType.YES) {
                try { noteEditor.save(); } catch (IOException ignored) {}
                return true;
            } else if (result.get() == javafx.scene.control.ButtonType.NO) {
                return true;
            }
        }
        return false;
    }

    private void openNoteByTitle(String title) {
        var vault = vaultManager.getVault();
        if (vault == null) return;
        var note = vault.getNote(title);
        if (note != null) {
            eventBus.publish(new NoteOpened(note));
        }
    }

    private void switchVault(app.orgx.desktop.model.VaultEntry entry) {
        try {
            openVault(entry.path());
            var config = configManager.load();
            config.setLastOpenedVault(entry.name());
            configManager.save(config);
        } catch (IOException ignored) {}
    }

    public void shutdown() throws IOException {
        if (fileWatcher != null) fileWatcher.stop();
        searchEngine.close();
    }
}
