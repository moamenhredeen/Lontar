package app.orgx.desktop.model;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Vault {
    private final Path rootPath;
    private final String name;
    private final Map<String, Note> notes;

    public Vault(Path rootPath) {
        this.rootPath = rootPath;
        this.name = rootPath.getFileName().toString();
        this.notes = new ConcurrentHashMap<>();
    }

    public Path rootPath() { return rootPath; }
    public String name() { return name; }
    public Map<String, Note> notes() { return notes; }

    public Note getNote(String title) { return notes.get(title.toLowerCase()); }
    public void putNote(Note note) { notes.put(note.title().toLowerCase(), note); }
    public void removeNote(String title) { notes.remove(title.toLowerCase()); }
}
