package app.orgx.desktop.editor;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteSaved;
import app.orgx.desktop.markdown.MarkdownToModel;
import app.orgx.desktop.markdown.ModelToMarkdown;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.vault.VaultManager;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;

import java.io.IOException;
import java.nio.file.Files;

public class NoteEditor {

    private final EventBus eventBus;
    private final VaultManager vaultManager;
    private final MarkdownToModel markdownToModel;
    private final ModelToMarkdown modelToMarkdown;

    private Note currentNote;
    private StyledTextModel currentModel;
    private boolean modified;
    private String originalContent;

    public NoteEditor(EventBus eventBus, VaultManager vaultManager) {
        this.eventBus = eventBus;
        this.vaultManager = vaultManager;
        this.markdownToModel = new MarkdownToModel();
        this.modelToMarkdown = new ModelToMarkdown();
    }

    public StyledTextModel open(Note note) {
        this.currentNote = note;
        this.originalContent = note.content();
        this.modified = false;

        var result = markdownToModel.convert(note.content());
        this.currentModel = result.model();
        return currentModel;
    }

    public void save() throws IOException {
        if (currentNote == null || currentModel == null) return;

        var markdown = modelToMarkdown.convert(currentModel);
        Files.writeString(currentNote.path(), markdown);
        currentNote.setContent(markdown);
        originalContent = markdown;
        modified = false;

        // Re-extract links and update vault
        var links = VaultManager.extractLinks(markdown);
        currentNote.setLinks(links);

        eventBus.publish(new NoteSaved(currentNote));
    }

    public void markModified() {
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public Note getCurrentNote() {
        return currentNote;
    }

    public StyledTextModel getCurrentModel() {
        return currentModel;
    }

    public void close() {
        currentNote = null;
        currentModel = null;
        modified = false;
        originalContent = null;
    }
}
