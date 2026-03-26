package app.orgx.desktop.vault;

import app.orgx.desktop.model.Link;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LinkIndexTest {
    private LinkIndex index;
    private Vault vault;

    @BeforeEach
    void setUp() {
        index = new LinkIndex();
        vault = new Vault(Path.of("/vault"));
    }

    private Note note(String title, String content, Set<String> links) {
        var n = new Note(Path.of("/vault/" + title + ".md"), title, content, Instant.now(), links);
        vault.putNote(n);
        return n;
    }

    @Test
    void backlinkIsFoundAfterRebuild() {
        note("A", "See [[B]] here", Set.of("B"));
        var noteB = note("B", "Just content", Set.of());
        index.rebuild(vault);
        var backlinks = index.getBacklinks(noteB);
        assertEquals(1, backlinks.size());
    }

    @Test
    void noBacklinksForUnlinkedNote() {
        note("A", "no links", Set.of());
        note("B", "also no links", Set.of());
        index.rebuild(vault);
        assertTrue(index.getBacklinks(vault.getNote("A")).isEmpty());
    }

    @Test
    void multipleBacklinks() {
        note("A", "link to [[C]]", Set.of("C"));
        note("B", "also links to [[C]]", Set.of("C"));
        var noteC = note("C", "popular note", Set.of());
        index.rebuild(vault);
        assertEquals(2, index.getBacklinks(noteC).size());
    }

    @Test
    void updateNoteRefreshesLinks() {
        var noteA = note("A", "link to [[B]]", Set.of("B"));
        var noteB = note("B", "content", Set.of());
        index.rebuild(vault);
        noteA.setContent("no more links");
        noteA.setLinks(Set.of());
        index.updateNote(noteA);
        assertTrue(index.getBacklinks(noteB).isEmpty());
    }

    @Test
    void backlinkContextContainsLinkLine() {
        note("A", "Line one\nSee [[B]] for info\nLine three", Set.of("B"));
        var noteB = note("B", "content", Set.of());
        index.rebuild(vault);
        var backlinks = index.getBacklinks(noteB);
        assertEquals(1, backlinks.size());
        assertTrue(backlinks.getFirst().context().contains("[[B]]"));
    }
}
