package app.orgx.desktop.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

public class Note {
    private final Path path;
    private String title;
    private String content;
    private Instant lastModified;
    private Set<String> links;

    public Note(Path path, String title, String content, Instant lastModified, Set<String> links) {
        this.path = path;
        this.title = title;
        this.content = content;
        this.lastModified = lastModified;
        this.links = links;
    }

    public Path path() { return path; }
    public String title() { return title; }
    public String content() { return content; }
    public Instant lastModified() { return lastModified; }
    public Set<String> links() { return links; }
    public void setContent(String content) { this.content = content; }
    public void setTitle(String title) { this.title = title; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
    public void setLinks(Set<String> links) { this.links = links; }
}
