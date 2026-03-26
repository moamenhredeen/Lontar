package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.LinkClicked;
import app.orgx.desktop.core.events.NoteOpened;
import app.orgx.desktop.core.events.NoteSaved;
import app.orgx.desktop.model.Link;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.vault.LinkIndex;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class BacklinksPanel extends VBox {

    private final EventBus eventBus;
    private final LinkIndex linkIndex;
    private final VBox cardContainer;
    private final Label headerLabel;

    public BacklinksPanel(EventBus eventBus, LinkIndex linkIndex) {
        this.eventBus = eventBus;
        this.linkIndex = linkIndex;
        getStyleClass().add("backlinks-panel");
        setPadding(new Insets(8));
        setSpacing(8);

        headerLabel = new Label("Backlinks");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        cardContainer = new VBox(8);
        var scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().addAll(headerLabel, scrollPane);
        scrollPane.prefHeightProperty().bind(heightProperty().subtract(40));

        eventBus.subscribe(NoteOpened.class, e -> Platform.runLater(() -> refresh(e.note())));
        eventBus.subscribe(NoteSaved.class, e -> Platform.runLater(() -> refresh(e.note())));
    }

    private void refresh(Note currentNote) {
        if (currentNote == null) {
            cardContainer.getChildren().clear();
            headerLabel.setText("Backlinks");
            return;
        }

        var backlinks = linkIndex.getBacklinks(currentNote);
        headerLabel.setText("Backlinks (" + backlinks.size() + ")");
        cardContainer.getChildren().clear();

        if (backlinks.isEmpty()) {
            var empty = new Label("No backlinks");
            empty.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            cardContainer.getChildren().add(empty);
            return;
        }

        for (var link : backlinks) {
            cardContainer.getChildren().add(createCard(link));
        }
    }

    private VBox createCard(Link link) {
        var card = new VBox(4);
        card.getStyleClass().add("backlink-card");

        var sourceName = link.source().getFileName().toString();
        var title = sourceName.endsWith(".md")
                ? sourceName.substring(0, sourceName.length() - 3)
                : sourceName;

        var titleLabel = new Label(title);
        titleLabel.getStyleClass().add("backlink-title");

        var contextLabel = new Label(link.context());
        contextLabel.getStyleClass().add("backlink-context");
        contextLabel.setWrapText(true);
        contextLabel.setMaxHeight(60);

        card.getChildren().addAll(titleLabel, contextLabel);
        card.setOnMouseClicked(e ->
                eventBus.publish(new LinkClicked(title)));

        return card;
    }
}
