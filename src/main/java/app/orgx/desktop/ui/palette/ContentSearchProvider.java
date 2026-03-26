package app.orgx.desktop.ui.palette;

import app.orgx.desktop.search.SearchEngine;
import javafx.scene.control.Label;

import java.util.List;
import java.util.function.Consumer;

public class ContentSearchProvider implements PaletteProvider {

    private final SearchEngine searchEngine;
    private final Consumer<String> onOpen;

    public ContentSearchProvider(SearchEngine searchEngine, Consumer<String> onOpen) {
        this.searchEngine = searchEngine;
        this.onOpen = onOpen;
    }

    @Override
    public boolean matches(String rawInput) {
        return rawInput.startsWith("?");
    }

    @Override
    public String stripPrefix(String rawInput) {
        return rawInput.substring(1).stripLeading();
    }

    @Override
    public List<PaletteResult> search(String query) {
        if (query.isBlank()) return List.of();
        try {
            return searchEngine.searchContent(query, 20).stream()
                    .map(r -> new PaletteResult(
                            new Label("\uD83D\uDCC4"),
                            r.title(),
                            r.snippet() != null ? r.snippet() : "",
                            () -> onOpen.accept(r.title())))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
