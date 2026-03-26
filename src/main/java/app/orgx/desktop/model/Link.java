package app.orgx.desktop.model;

import java.nio.file.Path;

public record Link(Path source, String target, int lineNumber, String context) {}
