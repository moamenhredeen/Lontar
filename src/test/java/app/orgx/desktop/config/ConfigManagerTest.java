package app.orgx.desktop.config;

import app.orgx.desktop.model.VaultConfig;
import app.orgx.desktop.model.VaultEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {
    @TempDir Path tempDir;
    private ConfigManager manager;

    @BeforeEach
    void setUp() { manager = new ConfigManager(tempDir.resolve("config.properties")); }

    @Test
    void loadReturnsDefaultsWhenNoFileExists() {
        var config = manager.load();
        assertNotNull(config);
        assertEquals("light", config.theme());
        assertFalse(config.showStatusBar());
        assertTrue(config.vaults().isEmpty());
    }

    @Test
    void saveAndLoadRoundTrips() {
        var config = new VaultConfig();
        config.setTheme("dark");
        config.setShowStatusBar(true);
        config.setLastOpenedVault("MyVault");
        config.setVaults(List.of(new VaultEntry("MyVault", Path.of("/home/user/notes"))));
        manager.save(config);

        var loaded = manager.load();
        assertEquals("dark", loaded.theme());
        assertTrue(loaded.showStatusBar());
        assertEquals("MyVault", loaded.lastOpenedVault());
        assertEquals(1, loaded.vaults().size());
        assertEquals("MyVault", loaded.vaults().getFirst().name());
        assertEquals(Path.of("/home/user/notes"), loaded.vaults().getFirst().path());
    }

    @Test
    void saveCreatesParentDirectories() {
        var nested = new ConfigManager(tempDir.resolve("a/b/config.properties"));
        nested.save(new VaultConfig());
        var loaded = nested.load();
        assertNotNull(loaded);
    }
}
