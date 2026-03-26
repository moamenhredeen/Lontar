package app.orgx.desktop.ui.palette;

import app.orgx.desktop.model.VaultEntry;
import javafx.scene.control.Label;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VaultSwitchProvider implements PaletteProvider {

    private final Supplier<List<VaultEntry>> vaultsSupplier;
    private final Consumer<VaultEntry> onSwitch;

    public VaultSwitchProvider(Supplier<List<VaultEntry>> vaultsSupplier, Consumer<VaultEntry> onSwitch) {
        this.vaultsSupplier = vaultsSupplier;
        this.onSwitch = onSwitch;
    }

    @Override
    public boolean matches(String rawInput) {
        return rawInput.startsWith("vault:");
    }

    @Override
    public String stripPrefix(String rawInput) {
        return rawInput.substring(6).stripLeading();
    }

    @Override
    public List<PaletteResult> search(String query) {
        var lowerQuery = query.toLowerCase();
        return vaultsSupplier.get().stream()
                .filter(v -> query.isBlank() || v.name().toLowerCase().contains(lowerQuery))
                .map(v -> new PaletteResult(
                        new Label("\uD83D\uDCC1"),
                        v.name(),
                        v.path().toString(),
                        () -> onSwitch.accept(v)))
                .toList();
    }
}
