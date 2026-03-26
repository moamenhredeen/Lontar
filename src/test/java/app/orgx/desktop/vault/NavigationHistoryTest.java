package app.orgx.desktop.vault;

import app.orgx.desktop.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class NavigationHistoryTest {
    private NavigationHistory history;

    @BeforeEach
    void setUp() { history = new NavigationHistory(); }

    private Note note(String title) {
        return new Note(Path.of("/vault/" + title + ".md"), title, "", Instant.now(), Set.of());
    }

    @Test
    void initiallyCannotGoBackOrForward() {
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
    }

    @Test
    void afterPushingTwoNotesCanGoBack() {
        history.push(note("A"));
        history.push(note("B"));
        assertTrue(history.canGoBack());
        var back = history.back();
        assertEquals("A", back.title());
    }

    @Test
    void goingBackThenForwardRestoresNote() {
        history.push(note("A"));
        history.push(note("B"));
        history.back();
        assertTrue(history.canGoForward());
        var forward = history.forward();
        assertEquals("B", forward.title());
    }

    @Test
    void pushingAfterBackClearsForwardHistory() {
        history.push(note("A"));
        history.push(note("B"));
        history.back();
        history.push(note("C"));
        assertFalse(history.canGoForward());
    }

    @Test
    void backOnSingleEntryReturnsNull() {
        history.push(note("A"));
        assertFalse(history.canGoBack());
        assertNull(history.back());
    }
}
