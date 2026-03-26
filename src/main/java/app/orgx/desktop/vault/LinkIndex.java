package app.orgx.desktop.vault;

import app.orgx.desktop.model.Link;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import java.util.*;
import java.util.regex.Pattern;

public class LinkIndex {
    private static final Pattern WIKILINK_PATTERN = Pattern.compile("\\[\\[([^\\]]+)]]");
    private final Map<String, List<Link>> backlinks = new HashMap<>();

    public void rebuild(Vault vault) {
        backlinks.clear();
        for (var note : vault.notes().values()) { indexNote(note); }
    }

    public void updateNote(Note note) {
        backlinks.values().forEach(links -> links.removeIf(link -> link.source().equals(note.path())));
        indexNote(note);
    }

    public void removeNote(Note note) {
        backlinks.values().forEach(links -> links.removeIf(link -> link.source().equals(note.path())));
        backlinks.remove(note.title().toLowerCase());
    }

    public List<Link> getBacklinks(Note note) {
        return backlinks.getOrDefault(note.title().toLowerCase(), List.of());
    }

    public List<Link> getOutgoingLinks(Note note) {
        var result = new ArrayList<Link>();
        var lines = note.content().split("\n");
        for (int i = 0; i < lines.length; i++) {
            var matcher = WIKILINK_PATTERN.matcher(lines[i]);
            while (matcher.find()) {
                result.add(new Link(note.path(), matcher.group(1), i + 1, lines[i]));
            }
        }
        return result;
    }

    private void indexNote(Note note) {
        var lines = note.content().split("\n");
        for (int i = 0; i < lines.length; i++) {
            var matcher = WIKILINK_PATTERN.matcher(lines[i]);
            while (matcher.find()) {
                var target = matcher.group(1).toLowerCase();
                backlinks.computeIfAbsent(target, k -> new ArrayList<>())
                    .add(new Link(note.path(), matcher.group(1), i + 1, lines[i]));
            }
        }
    }
}
