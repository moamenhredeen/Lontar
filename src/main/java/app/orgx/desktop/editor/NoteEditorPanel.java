package app.orgx.desktop.editor;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteOpened;
import app.orgx.desktop.model.Note;
import javafx.application.Platform;
import jfx.incubator.scene.control.richtext.RichTextArea;
import javafx.scene.layout.StackPane;

public class NoteEditorPanel extends StackPane {

    private final RichTextArea richTextArea;
    private final NoteEditor noteEditor;
    private final EventBus eventBus;

    public NoteEditorPanel(EventBus eventBus, NoteEditor noteEditor) {
        this.eventBus = eventBus;
        this.noteEditor = noteEditor;
        getStyleClass().add("note-editor");

        richTextArea = new RichTextArea();
        richTextArea.setWrapText(true);

        getChildren().add(richTextArea);

        subscribeToEvents();
    }

    private void subscribeToEvents() {
        eventBus.subscribe(NoteOpened.class, e -> Platform.runLater(() -> openNote(e.note())));
    }

    private void openNote(Note note) {
        if (note == null) return;
        var model = noteEditor.open(note);
        richTextArea.setModel(model);
    }

    public RichTextArea getRichTextArea() {
        return richTextArea;
    }
}
