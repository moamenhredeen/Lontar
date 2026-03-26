package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.CommandPaletteClosed;
import app.orgx.desktop.core.events.CommandPaletteOpened;
import app.orgx.desktop.ui.palette.PaletteProvider;
import app.orgx.desktop.ui.palette.PaletteResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.util.List;

public class CommandPalette extends VBox {

    private final TextField searchField;
    private final ListView<PaletteResult> resultsList;
    private final List<PaletteProvider> providers;
    private final EventBus eventBus;

    public CommandPalette(EventBus eventBus, List<PaletteProvider> providers) {
        this.eventBus = eventBus;
        this.providers = providers;

        getStyleClass().add("command-palette");
        setMaxWidth(600);
        setMaxHeight(400);
        setAlignment(Pos.TOP_CENTER);

        searchField = new TextField();
        searchField.setPromptText("Search notes, commands, vaults...");

        resultsList = new ListView<>();
        resultsList.setCellFactory(lv -> new PaletteResultCell());
        resultsList.setMaxHeight(350);

        getChildren().addAll(searchField, resultsList);

        searchField.textProperty().addListener((obs, old, text) -> updateResults(text));

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hide();
            } else if (e.getCode() == KeyCode.ENTER) {
                executeSelected();
            } else if (e.getCode() == KeyCode.DOWN) {
                resultsList.requestFocus();
                if (!resultsList.getItems().isEmpty()) {
                    resultsList.getSelectionModel().selectFirst();
                }
                e.consume();
            }
        });

        resultsList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hide();
            } else if (e.getCode() == KeyCode.ENTER) {
                executeSelected();
            }
        });

        resultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                executeSelected();
            }
        });

        setVisible(false);
        setManaged(false);
    }

    public void show() {
        searchField.clear();
        resultsList.getItems().clear();
        setVisible(true);
        setManaged(true);
        searchField.requestFocus();
        eventBus.publish(new CommandPaletteOpened());
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        eventBus.publish(new CommandPaletteClosed());
    }

    public boolean isShowing() {
        return isVisible();
    }

    private void updateResults(String rawInput) {
        resultsList.getItems().clear();
        if (rawInput == null || rawInput.isBlank()) return;

        for (var provider : providers) {
            if (provider.matches(rawInput)) {
                var query = provider.stripPrefix(rawInput);
                resultsList.getItems().addAll(provider.search(query));
                break;
            }
        }

        if (!resultsList.getItems().isEmpty()) {
            resultsList.getSelectionModel().selectFirst();
        }
    }

    private void executeSelected() {
        var selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            hide();
            selected.action().run();
        }
    }

    private static class PaletteResultCell extends ListCell<PaletteResult> {
        @Override
        protected void updateItem(PaletteResult result, boolean empty) {
            super.updateItem(result, empty);
            if (empty || result == null) {
                setGraphic(null);
                return;
            }

            var titleLabel = new Label(result.title());
            titleLabel.getStyleClass().add("palette-result-title");

            var subtitleLabel = new Label(result.subtitle());
            subtitleLabel.getStyleClass().add("palette-result-subtitle");

            var textBox = new VBox(2, titleLabel, subtitleLabel);
            var row = new HBox(8, result.icon(), textBox);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 8, 4, 8));

            setGraphic(row);
        }
    }
}
