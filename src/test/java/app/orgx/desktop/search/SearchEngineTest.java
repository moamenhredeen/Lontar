package app.orgx.desktop.search;

import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {

    private SearchEngine engine;
    private Vault vault;

    @BeforeEach
    void setUp() throws Exception {
        engine = new SearchEngine();
        vault = new Vault(Path.of("/vault"));
    }

    @AfterEach
    void tearDown() throws Exception {
        engine.close();
    }

    private void addNote(String title, String content) {
        vault.putNote(new Note(
                Path.of("/vault/" + title + ".md"), title, content, Instant.now(), Set.of()));
    }

    @Test
    void searchByTitleFindsNote() throws Exception {
        addNote("Meeting Notes", "Some meeting content");
        addNote("Shopping List", "Milk, eggs");
        engine.rebuildIndex(vault);

        var results = engine.searchByTitle("meeting", 10);
        assertEquals(1, results.size());
        assertEquals("Meeting Notes", results.getFirst().title());
    }

    @Test
    void searchContentFindsMatchingNotes() throws Exception {
        addNote("Note A", "The deployment pipeline needs fixing");
        addNote("Note B", "Shopping list for today");
        engine.rebuildIndex(vault);

        var results = engine.searchContent("deployment pipeline", 10);
        assertEquals(1, results.size());
        assertEquals("Note A", results.getFirst().title());
    }

    @Test
    void searchContentReturnsSnippet() throws Exception {
        addNote("Note A", "First line\nThe deployment pipeline needs fixing\nLast line");
        engine.rebuildIndex(vault);

        var results = engine.searchContent("deployment", 10);
        assertFalse(results.isEmpty());
        assertNotNull(results.getFirst().snippet());
        // Snippet should contain the matching term
        assertTrue(results.getFirst().snippet().toLowerCase().contains("deployment"));
    }

    @Test
    void fuzzyTitleSearchToleratesTypos() throws Exception {
        addNote("Obsidian", "Note about obsidian");
        engine.rebuildIndex(vault);

        var results = engine.searchByTitle("obsidan", 10);  // typo
        assertFalse(results.isEmpty());
        assertEquals("Obsidian", results.getFirst().title());
    }

    @Test
    void incrementalUpdateIndexesSingleNote() throws Exception {
        addNote("Original", "original content");
        engine.rebuildIndex(vault);

        var newNote = new Note(Path.of("/vault/New.md"), "New", "brand new note", Instant.now(), Set.of());
        engine.updateNote(newNote);

        var results = engine.searchContent("brand new", 10);
        assertEquals(1, results.size());
        assertEquals("New", results.getFirst().title());
    }

    @Test
    void deleteNoteRemovesFromIndex() throws Exception {
        addNote("ToDelete", "delete me");
        engine.rebuildIndex(vault);

        engine.deleteNote(Path.of("/vault/ToDelete.md"));

        var results = engine.searchContent("delete me", 10);
        assertTrue(results.isEmpty());
    }
}
