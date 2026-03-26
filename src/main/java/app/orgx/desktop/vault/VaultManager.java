package app.orgx.desktop.vault;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.*;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VaultManager {

    private static final Pattern WIKILINK_PATTERN = Pattern.compile("\\[\\[([^\\]]+)]]");

    private final EventBus eventBus;
    private Vault vault;

    public VaultManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void openVault(Path rootPath) throws IOException {
        vault = new Vault(rootPath);
        scanVault(rootPath);
        eventBus.publish(new VaultOpened(vault));
    }

    public void closeVault() {
        vault = null;
        eventBus.publish(new VaultClosed());
    }

    public Vault getVault() {
        return vault;
    }

    public Note createNote(String title) throws IOException {
        if (vault == null) throw new IllegalStateException("No vault open");
        var path = vault.rootPath().resolve(title + ".md");
        Files.writeString(path, "");
        var note = new Note(path, title, "", Instant.now(), Set.of());
        vault.putNote(note);
        eventBus.publish(new NoteCreated(path));
        return note;
    }

    public void deleteNote(Path path) throws IOException {
        if (vault == null) throw new IllegalStateException("No vault open");
        var title = fileNameToTitle(path);
        Files.deleteIfExists(path);
        vault.removeNote(title);
        eventBus.publish(new NoteDeleted(path));
    }

    public void renameNote(Path oldPath, String newTitle) throws IOException {
        if (vault == null) throw new IllegalStateException("No vault open");
        var oldTitle = fileNameToTitle(oldPath);
        var newPath = oldPath.resolveSibling(newTitle + ".md");
        Files.move(oldPath, newPath);
        vault.removeNote(oldTitle);

        var content = Files.readString(newPath);
        var note = new Note(newPath, newTitle, content,
                Files.getLastModifiedTime(newPath).toInstant(), extractLinks(content));
        vault.putNote(note);
        eventBus.publish(new NoteRenamed(oldPath, newPath));
    }

    public Note loadNote(Path path) throws IOException {
        var content = Files.readString(path);
        var title = fileNameToTitle(path);
        var note = vault.getNote(title);
        if (note != null) {
            note.setContent(content);
            note.setLastModified(Files.getLastModifiedTime(path).toInstant());
            note.setLinks(extractLinks(content));
        }
        return note;
    }

    private void scanVault(Path root) throws IOException {
        try (var walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".md"))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        var content = Files.readString(p);
                        var title = fileNameToTitle(p);
                        var note = new Note(p, title, content,
                                Files.getLastModifiedTime(p).toInstant(),
                                extractLinks(content));
                        vault.putNote(note);
                    } catch (IOException e) {
                        // Skip unreadable files
                    }
                });
        }
    }

    public static String fileNameToTitle(Path path) {
        var name = path.getFileName().toString();
        return name.endsWith(".md") ? name.substring(0, name.length() - 3) : name;
    }

    public static Set<String> extractLinks(String content) {
        var matcher = WIKILINK_PATTERN.matcher(content);
        return matcher.results()
                .map(r -> r.group(1))
                .collect(Collectors.toSet());
    }
}
