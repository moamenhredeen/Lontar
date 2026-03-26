package app.orgx.desktop.ui.palette;

import java.util.List;

public interface PaletteProvider {
    boolean matches(String rawInput);
    List<PaletteResult> search(String query);
    default String stripPrefix(String rawInput) { return rawInput; }
}
