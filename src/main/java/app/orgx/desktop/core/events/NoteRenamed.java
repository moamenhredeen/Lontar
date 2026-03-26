package app.orgx.desktop.core.events;

import java.nio.file.Path;

public record NoteRenamed(Path oldPath, Path newPath) {}
