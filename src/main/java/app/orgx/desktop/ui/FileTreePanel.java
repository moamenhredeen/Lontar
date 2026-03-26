package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.*;
import app.orgx.desktop.model.Vault;
import app.orgx.desktop.vault.VaultManager;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileTreePanel extends VBox {

    private final TreeView<Path> treeView;
    private final EventBus eventBus;
    private final VaultManager vaultManager;
    private Vault vault;

    public FileTreePanel(EventBus eventBus, VaultManager vaultManager) {
        this.eventBus = eventBus;
        this.vaultManager = vaultManager;
        this.treeView = new TreeView<>();
        getStyleClass().add("file-tree");

        treeView.setShowRoot(true);
        treeView.setCellFactory(tv -> new FileTreeCell());

        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                var selected = treeView.getSelectionModel().getSelectedItem();
                if (selected != null && Files.isRegularFile(selected.getValue())) {
                    eventBus.publish(new NoteOpened(
                            vault.getNote(fileToTitle(selected.getValue()))));
                }
            }
        });

        getChildren().add(treeView);
        treeView.prefHeightProperty().bind(heightProperty());

        setupContextMenu();
        subscribeToEvents();
    }

    public void loadVault(Vault vault) {
        this.vault = vault;
        var root = createTreeItem(vault.rootPath());
        root.setExpanded(true);
        treeView.setRoot(root);
    }

    private TreeItem<Path> createTreeItem(Path dir) {
        var item = new TreeItem<>(dir);
        try (var stream = Files.list(dir)) {
            stream.sorted(Comparator
                    .<Path, Boolean>comparing(p -> !Files.isDirectory(p))
                    .thenComparing(p -> p.getFileName().toString().toLowerCase()))
                .forEach(p -> {
                    if (Files.isDirectory(p)) {
                        item.getChildren().add(createTreeItem(p));
                    } else if (p.toString().endsWith(".md")) {
                        item.getChildren().add(new TreeItem<>(p));
                    }
                });
        } catch (IOException ignored) {}
        return item;
    }

    private void setupContextMenu() {
        var newNote = new MenuItem("New Note");
        newNote.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var dialog = new TextInputDialog("Untitled");
            dialog.setTitle("New Note");
            dialog.setHeaderText("Enter note name:");
            dialog.showAndWait().ifPresent(name -> {
                try {
                    vaultManager.createNote(name);
                } catch (IOException ignored) {}
            });
        });

        var newFolder = new MenuItem("New Folder");
        newFolder.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var dir = Files.isDirectory(selected.getValue())
                    ? selected.getValue()
                    : selected.getValue().getParent();
            var dialog = new TextInputDialog("New Folder");
            dialog.setTitle("New Folder");
            dialog.setHeaderText("Enter folder name:");
            dialog.showAndWait().ifPresent(name -> {
                try {
                    Files.createDirectories(dir.resolve(name));
                    loadVault(vault); // refresh tree
                } catch (IOException ignored) {}
            });
        });

        var delete = new MenuItem("Delete");
        delete.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + selected.getValue().getFileName() + "?");
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        vaultManager.deleteNote(selected.getValue());
                    } catch (IOException ignored) {}
                }
            });
        });

        var reveal = new MenuItem("Reveal in File Manager");
        reveal.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    var desktop = java.awt.Desktop.getDesktop();
                    desktop.open(selected.getValue().getParent().toFile());
                } catch (Exception ignored) {}
            }
        });

        treeView.setContextMenu(new ContextMenu(newNote, newFolder, delete, new SeparatorMenuItem(), reveal));
    }

    private void subscribeToEvents() {
        eventBus.subscribe(NoteCreated.class, e -> Platform.runLater(() -> {
            if (vault != null) loadVault(vault);
        }));
        eventBus.subscribe(NoteDeleted.class, e -> Platform.runLater(() -> {
            if (vault != null) loadVault(vault);
        }));
        eventBus.subscribe(NoteRenamed.class, e -> Platform.runLater(() -> {
            if (vault != null) loadVault(vault);
        }));
    }

    private String fileToTitle(Path path) {
        var name = path.getFileName().toString();
        return name.endsWith(".md") ? name.substring(0, name.length() - 3) : name;
    }

    private static class FileTreeCell extends TreeCell<Path> {
        @Override
        protected void updateItem(Path path, boolean empty) {
            super.updateItem(path, empty);
            if (empty || path == null) {
                setText(null);
                setGraphic(null);
            } else if (Files.isDirectory(path)) {
                setText(path.getFileName().toString());
            } else {
                var name = path.getFileName().toString();
                setText(name.endsWith(".md") ? name.substring(0, name.length() - 3) : name);
            }
        }
    }
}
