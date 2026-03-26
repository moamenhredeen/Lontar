package app.orgx.desktop.vault;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteCreated;
import app.orgx.desktop.core.events.NoteDeleted;
import app.orgx.desktop.core.events.VaultOpened;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class VaultManagerTest {

    @TempDir
    Path vaultDir;

    private EventBus bus;
    private VaultManager manager;

    @BeforeEach
    void setUp() {
        bus = new EventBus();
        manager = new VaultManager(bus);
    }

    private void createFile(String name, String content) throws IOException {
        Files.writeString(vaultDir.resolve(name), content);
    }

    @Test
    void openVaultScansMarkdownFiles() throws IOException {
        createFile("note1.md", "# Note 1\nSome text");
        createFile("note2.md", "# Note 2\nMore text");
        createFile("readme.txt", "not a note");

        manager.openVault(vaultDir);

        var vault = manager.getVault();
        assertNotNull(vault);
        assertEquals(2, vault.notes().size());
        assertNotNull(vault.getNote("note1"));
        assertNotNull(vault.getNote("note2"));
    }

    @Test
    void openVaultFiresVaultOpenedEvent() throws IOException {
        createFile("test.md", "hello");
        var events = new ArrayList<VaultOpened>();
        bus.subscribe(VaultOpened.class, events::add);

        manager.openVault(vaultDir);

        assertEquals(1, events.size());
        assertEquals(vaultDir, events.getFirst().vault().rootPath());
    }

    @Test
    void openVaultScansSubdirectories() throws IOException {
        Files.createDirectories(vaultDir.resolve("sub"));
        createFile("sub/nested.md", "nested note");

        manager.openVault(vaultDir);

        assertNotNull(manager.getVault().getNote("nested"));
    }

    @Test
    void createNoteCreatesFileAndAddsToVault() throws IOException {
        manager.openVault(vaultDir);

        var events = new ArrayList<NoteCreated>();
        bus.subscribe(NoteCreated.class, events::add);

        var note = manager.createNote("New Note");

        assertTrue(Files.exists(vaultDir.resolve("New Note.md")));
        assertNotNull(manager.getVault().getNote("New Note"));
        assertEquals(1, events.size());
    }

    @Test
    void deleteNoteRemovesFileAndFromVault() throws IOException {
        createFile("doomed.md", "goodbye");
        manager.openVault(vaultDir);

        var events = new ArrayList<NoteDeleted>();
        bus.subscribe(NoteDeleted.class, events::add);

        manager.deleteNote(vaultDir.resolve("doomed.md"));

        assertFalse(Files.exists(vaultDir.resolve("doomed.md")));
        assertNull(manager.getVault().getNote("doomed"));
        assertEquals(1, events.size());
    }

    @Test
    void getNoteBytitleIsCaseInsensitive() throws IOException {
        createFile("My Note.md", "content");
        manager.openVault(vaultDir);

        assertNotNull(manager.getVault().getNote("my note"));
        assertNotNull(manager.getVault().getNote("MY NOTE"));
    }

    @Test
    void noteTitleIsFilenameWithoutExtension() throws IOException {
        createFile("Hello World.md", "content");
        manager.openVault(vaultDir);

        var note = manager.getVault().getNote("Hello World");
        assertEquals("Hello World", note.title());
    }
}
