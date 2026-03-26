package app.orgx.desktop.model;

import java.util.ArrayList;
import java.util.List;

public class VaultConfig {
    private List<VaultEntry> vaults = new ArrayList<>();
    private String lastOpenedVault = "";
    private String lastOpenedNote = "";
    private boolean showStatusBar = false;
    private String theme = "light";
    private boolean fileTreeVisible = true;
    private boolean backlinksVisible = true;

    public List<VaultEntry> vaults() { return vaults; }
    public String lastOpenedVault() { return lastOpenedVault; }
    public String lastOpenedNote() { return lastOpenedNote; }
    public boolean showStatusBar() { return showStatusBar; }
    public String theme() { return theme; }
    public boolean fileTreeVisible() { return fileTreeVisible; }
    public boolean backlinksVisible() { return backlinksVisible; }
    public void setVaults(List<VaultEntry> vaults) { this.vaults = vaults; }
    public void setLastOpenedVault(String v) { this.lastOpenedVault = v; }
    public void setLastOpenedNote(String n) { this.lastOpenedNote = n; }
    public void setShowStatusBar(boolean v) { this.showStatusBar = v; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setFileTreeVisible(boolean v) { this.fileTreeVisible = v; }
    public void setBacklinksVisible(boolean v) { this.backlinksVisible = v; }
}
