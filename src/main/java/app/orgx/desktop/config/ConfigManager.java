package app.orgx.desktop.config;

import app.orgx.desktop.model.VaultConfig;
import app.orgx.desktop.model.VaultEntry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

public class ConfigManager {
    private final Path configPath;

    public ConfigManager(Path configPath) { this.configPath = configPath; }

    public static ConfigManager createDefault() {
        return new ConfigManager(Path.of(System.getProperty("user.home"), ".config", "orgx", "config.properties"));
    }

    public VaultConfig load() {
        var config = new VaultConfig();
        if (!Files.exists(configPath)) return config;
        var props = new Properties();
        try (var reader = Files.newBufferedReader(configPath)) {
            props.load(reader);
        } catch (IOException e) { return config; }

        config.setTheme(props.getProperty("theme", "light"));
        config.setShowStatusBar(Boolean.parseBoolean(props.getProperty("showStatusBar", "false")));
        config.setLastOpenedVault(props.getProperty("lastOpenedVault", ""));
        config.setLastOpenedNote(props.getProperty("lastOpenedNote", ""));
        config.setFileTreeVisible(Boolean.parseBoolean(props.getProperty("fileTreeVisible", "true")));
        config.setBacklinksVisible(Boolean.parseBoolean(props.getProperty("backlinksVisible", "true")));

        var vaults = new ArrayList<VaultEntry>();
        for (int i = 0; ; i++) {
            var name = props.getProperty("vault." + i + ".name");
            var path = props.getProperty("vault." + i + ".path");
            if (name == null || path == null) break;
            vaults.add(new VaultEntry(name, Path.of(path)));
        }
        config.setVaults(vaults);
        return config;
    }

    public void save(VaultConfig config) {
        try { Files.createDirectories(configPath.getParent()); } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory", e);
        }
        var props = new Properties();
        props.setProperty("theme", config.theme());
        props.setProperty("showStatusBar", String.valueOf(config.showStatusBar()));
        props.setProperty("lastOpenedVault", config.lastOpenedVault());
        props.setProperty("lastOpenedNote", config.lastOpenedNote());
        props.setProperty("fileTreeVisible", String.valueOf(config.fileTreeVisible()));
        props.setProperty("backlinksVisible", String.valueOf(config.backlinksVisible()));
        var vaults = config.vaults();
        for (int i = 0; i < vaults.size(); i++) {
            props.setProperty("vault." + i + ".name", vaults.get(i).name());
            props.setProperty("vault." + i + ".path", vaults.get(i).path().toString());
        }
        try (var writer = Files.newBufferedWriter(configPath)) {
            props.store(writer, "orgx configuration");
        } catch (IOException e) { throw new RuntimeException("Failed to save config", e); }
    }
}
