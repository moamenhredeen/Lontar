package app.orgx.desktop.search;

import java.nio.file.Path;

public record SearchResult(Path path, String title, String snippet, float score) {}
