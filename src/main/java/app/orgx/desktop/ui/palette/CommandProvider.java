package app.orgx.desktop.ui.palette;

import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class CommandProvider implements PaletteProvider {

    public record Command(String name, String description, Runnable action) {}

    private final List<Command> commands = new ArrayList<>();

    public void register(String name, String description, Runnable action) {
        commands.add(new Command(name, description, action));
    }

    @Override
    public boolean matches(String rawInput) {
        return rawInput.startsWith(">");
    }

    @Override
    public String stripPrefix(String rawInput) {
        return rawInput.substring(1).stripLeading();
    }

    @Override
    public List<PaletteResult> search(String query) {
        var lowerQuery = query.toLowerCase();
        return commands.stream()
                .filter(c -> query.isBlank() || c.name().toLowerCase().contains(lowerQuery))
                .map(c -> new PaletteResult(
                        new Label(">"),
                        c.name(),
                        c.description(),
                        c.action()))
                .toList();
    }
}
