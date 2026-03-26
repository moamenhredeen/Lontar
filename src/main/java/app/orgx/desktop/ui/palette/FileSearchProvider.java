package app.orgx.desktop.ui.palette;

import app.orgx.desktop.search.SearchEngine;
import javafx.scene.control.Label;

import java.util.List;
import java.util.function.Consumer;

public class FileSearchProvider implements PaletteProvider {

    private final SearchEngine searchEngine;
    private final Consumer<String> onOpen;

    public FileSearchProvider(SearchEngine searchEngine, Consumer<String> onOpen) {
        this.searchEngine = searchEngine;
        this.onOpen = onOpen;
    }

    @Override
    public boolean matches(String rawInput) {
        // Default provider — matches when no prefix is used
        return !rawInput.startsWith("?") && !rawInput.startsWith(">") && !rawInput.startsWith("vault:");
    }

    @Override
    public List<PaletteResult> search(String query) {
        if (query.isBlank()) return List.of();
        try {
            return searchEngine.searchByTitle(query, 20).stream()
                    .map(r -> new PaletteResult(
                            new Label("\uD83D\uDCC4"),
                            r.title(),
                            r.path().getParent().getFileName().toString(),
                            () -> onOpen.accept(r.title())))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
