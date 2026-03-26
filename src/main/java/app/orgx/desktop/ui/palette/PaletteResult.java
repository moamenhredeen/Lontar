package app.orgx.desktop.ui.palette;

import javafx.scene.Node;

public record PaletteResult(Node icon, String title, String subtitle, Runnable action) {}
