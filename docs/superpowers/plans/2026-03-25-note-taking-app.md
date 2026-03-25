# Orgx Note-Taking App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Obsidian-like note-taking desktop app with WYSIWYG markdown editing, wikilinks, backlinks, Lucene search, and command palette — all on JavaFX 26 with RichTextArea.

**Architecture:** Event-driven services communicating via an EventBus. Plain markdown files on disk, parsed with commonmark-java, rendered through RichTextArea's RichTextModel. Lucene powers search. Command palette is the primary interaction hub.

**Tech Stack:** Java 26, JavaFX 26 (RichTextArea), commonmark-java 0.24.0, Apache Lucene 10.x, Log4j2, JUnit 5

**Spec:** `docs/superpowers/specs/2026-03-25-note-taking-app-design.md`

**RichTextArea reference:** `/home/moamen/git-repos/jfx/apps/samples/RichTextAreaDemo/src/com/oracle/demo/richtext/`

**IMPORTANT — RichTextArea API Notes:**
- RichTextArea is in the **incubator module**: `jfx.incubator.richtext`
- Correct imports:
  - `jfx.incubator.scene.control.richtext.RichTextArea`
  - `jfx.incubator.scene.control.richtext.model.RichTextModel`
  - `jfx.incubator.scene.control.richtext.model.StyleAttributeMap`
  - `jfx.incubator.scene.control.richtext.model.StyledTextModel`
- The `StyleAttributeMap.builder()` API uses `.setBold(true)`, `.setItalic(true)`, `.setFontSize(24)`, `.setFontFamily("Monospaced")`, `.setTextColor(Color)`, etc.
- `pom.xml` needs the `javafx-incubator` artifact (or equivalent) from OpenJFX 26
- `module-info.java` needs `requires jfx.incubator.richtext;`
- All code examples in this plan that reference RichTextArea classes MUST use `jfx.incubator.scene.control.richtext.*` imports, NOT `javafx.scene.control.*` or `javafx.scene.text.*`

---

## File Map

### Files to modify
- `pom.xml` — add commonmark-java, Lucene dependencies
- `src/main/java/module-info.java` — add module requires, fix opens
- `src/main/java/app/orgx/desktop/OrgxApplication.java` — replace HeaderBar with AppShell
- `src/main/java/app/orgx/desktop/Launcher.java` — no changes needed

### Files to create

**Core:**
- `src/main/java/app/orgx/desktop/core/EventBus.java`
- `src/main/java/app/orgx/desktop/core/AppContext.java`
- `src/main/java/app/orgx/desktop/core/events/VaultOpened.java`
- `src/main/java/app/orgx/desktop/core/events/VaultClosed.java`
- `src/main/java/app/orgx/desktop/core/events/NoteOpened.java`
- `src/main/java/app/orgx/desktop/core/events/NoteSaved.java`
- `src/main/java/app/orgx/desktop/core/events/NoteCreated.java`
- `src/main/java/app/orgx/desktop/core/events/NoteDeleted.java`
- `src/main/java/app/orgx/desktop/core/events/NoteRenamed.java`
- `src/main/java/app/orgx/desktop/core/events/NoteExternallyChanged.java`
- `src/main/java/app/orgx/desktop/core/events/LinkClicked.java`
- `src/main/java/app/orgx/desktop/core/events/ThemeChanged.java`
- `src/main/java/app/orgx/desktop/core/events/SettingChanged.java`
- `src/main/java/app/orgx/desktop/core/events/CommandPaletteOpened.java`
- `src/main/java/app/orgx/desktop/core/events/CommandPaletteClosed.java`
- `src/main/java/app/orgx/desktop/core/events/SearchRequested.java`

**Model:**
- `src/main/java/app/orgx/desktop/model/Note.java`
- `src/main/java/app/orgx/desktop/model/Vault.java`
- `src/main/java/app/orgx/desktop/model/Link.java`
- `src/main/java/app/orgx/desktop/model/VaultEntry.java`
- `src/main/java/app/orgx/desktop/model/VaultConfig.java`

**Vault services:**
- `src/main/java/app/orgx/desktop/vault/VaultManager.java`
- `src/main/java/app/orgx/desktop/vault/LinkIndex.java`
- `src/main/java/app/orgx/desktop/vault/FileWatcher.java`
- `src/main/java/app/orgx/desktop/vault/NavigationHistory.java`

**Markdown:**
- `src/main/java/app/orgx/desktop/markdown/WikiLinkExtension.java`
- `src/main/java/app/orgx/desktop/markdown/WikiLinkNode.java`
- `src/main/java/app/orgx/desktop/markdown/MarkdownToModel.java`
- `src/main/java/app/orgx/desktop/markdown/ModelToMarkdown.java`

**Editor:**
- `src/main/java/app/orgx/desktop/editor/NoteEditor.java`
- `src/main/java/app/orgx/desktop/editor/NoteEditorPanel.java`

**Search:**
- `src/main/java/app/orgx/desktop/search/SearchEngine.java`
- `src/main/java/app/orgx/desktop/search/SearchResult.java`

**Config:**
- `src/main/java/app/orgx/desktop/config/ConfigManager.java`

**UI:**
- `src/main/java/app/orgx/desktop/ui/AppShell.java`
- `src/main/java/app/orgx/desktop/ui/FileTreePanel.java`
- `src/main/java/app/orgx/desktop/ui/BacklinksPanel.java`
- `src/main/java/app/orgx/desktop/ui/StatusBar.java`
- `src/main/java/app/orgx/desktop/ui/CommandPalette.java`
- `src/main/java/app/orgx/desktop/ui/palette/PaletteProvider.java`
- `src/main/java/app/orgx/desktop/ui/palette/PaletteResult.java`
- `src/main/java/app/orgx/desktop/ui/palette/FileSearchProvider.java`
- `src/main/java/app/orgx/desktop/ui/palette/ContentSearchProvider.java`
- `src/main/java/app/orgx/desktop/ui/palette/CommandProvider.java`
- `src/main/java/app/orgx/desktop/ui/palette/VaultSwitchProvider.java`

**Themes:**
- `src/main/resources/themes/light.css`
- `src/main/resources/themes/dark.css`

**Tests:**
- `src/test/java/app/orgx/desktop/core/EventBusTest.java`
- `src/test/java/app/orgx/desktop/vault/VaultManagerTest.java`
- `src/test/java/app/orgx/desktop/vault/LinkIndexTest.java`
- `src/test/java/app/orgx/desktop/vault/NavigationHistoryTest.java`
- `src/test/java/app/orgx/desktop/markdown/WikiLinkExtensionTest.java`
- `src/test/java/app/orgx/desktop/markdown/MarkdownToModelTest.java`
- `src/test/java/app/orgx/desktop/markdown/ModelToMarkdownTest.java`
- `src/test/java/app/orgx/desktop/search/SearchEngineTest.java`
- `src/test/java/app/orgx/desktop/config/ConfigManagerTest.java`

---

### Task 1: Project Setup — Dependencies & Module Config

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/module-info.java`

- [ ] **Step 1: Add commonmark-java dependency to pom.xml**

Add after the log4j dependencies block:

```xml
<!-- markdown parsing -->
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.24.0</version>
</dependency>
```

- [ ] **Step 2: Add Lucene dependencies to pom.xml**

Add after commonmark:

```xml
<!-- search -->
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>10.2.1</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-queryparser</artifactId>
    <version>10.2.1</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-highlighter</artifactId>
    <version>10.2.1</version>
</dependency>
```

- [ ] **Step 3: Update module-info.java**

Replace the entire file with:

```java
module app.orgx.desktop {
    requires javafx.controls;
    requires jfx.incubator.richtext;
    requires org.apache.logging.log4j;
    requires org.commonmark;
    requires org.apache.lucene.core;
    requires org.apache.lucene.queryparser;
    requires org.apache.lucene.highlighter;

    opens app.orgx.desktop to javafx.graphics;
    opens app.orgx.desktop.core to app.orgx.desktop;
    opens app.orgx.desktop.core.events to app.orgx.desktop;
    opens app.orgx.desktop.model to app.orgx.desktop;

    exports app.orgx.desktop;
    exports app.orgx.desktop.core;
    exports app.orgx.desktop.core.events;
    exports app.orgx.desktop.model;
    exports app.orgx.desktop.vault;
    exports app.orgx.desktop.markdown;
    exports app.orgx.desktop.editor;
    exports app.orgx.desktop.search;
    exports app.orgx.desktop.config;
    exports app.orgx.desktop.ui;
    exports app.orgx.desktop.ui.palette;
}
```

Note: The exact module names for Lucene and the incubator depend on the actual module descriptors in the JARs. Check by running `jar --describe-module --file=` on the downloaded JARs if compilation fails, and adjust accordingly. Lucene 10.x may use automatic module names. The incubator artifact may need to be added separately — check if `javafx-controls:26` bundles it or if a separate `javafx-incubator:26` artifact is needed in pom.xml.

- [ ] **Step 4: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS (no source changes yet, just dependency resolution + module config)

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/java/module-info.java
git commit -m "Add commonmark-java and Lucene dependencies, fix module-info"
```

---

### Task 2: EventBus & Event Records

**Files:**
- Create: `src/main/java/app/orgx/desktop/core/EventBus.java`
- Create: `src/main/java/app/orgx/desktop/core/events/*.java` (all event records)
- Create: `src/test/java/app/orgx/desktop/core/EventBusTest.java`

- [ ] **Step 1: Write EventBus tests**

```java
package app.orgx.desktop.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    record TestEvent(String message) {}
    record OtherEvent(int value) {}

    private EventBus bus;

    @BeforeEach
    void setUp() {
        bus = new EventBus();
    }

    @Test
    void subscriberReceivesPublishedEvent() {
        var received = new ArrayList<TestEvent>();
        bus.subscribe(TestEvent.class, received::add);
        bus.publish(new TestEvent("hello"));
        assertEquals(1, received.size());
        assertEquals("hello", received.getFirst().message());
    }

    @Test
    void subscriberDoesNotReceiveUnrelatedEvents() {
        var received = new ArrayList<TestEvent>();
        bus.subscribe(TestEvent.class, received::add);
        bus.publish(new OtherEvent(42));
        assertTrue(received.isEmpty());
    }

    @Test
    void multipleSubscribersAllReceiveEvent() {
        var list1 = new ArrayList<TestEvent>();
        var list2 = new ArrayList<TestEvent>();
        bus.subscribe(TestEvent.class, list1::add);
        bus.subscribe(TestEvent.class, list2::add);
        bus.publish(new TestEvent("hi"));
        assertEquals(1, list1.size());
        assertEquals(1, list2.size());
    }

    @Test
    void unsubscribedHandlerDoesNotReceiveEvents() {
        var received = new ArrayList<TestEvent>();
        // Store handler reference — method references create new instances each time
        Consumer<TestEvent> handler = received::add;
        bus.subscribe(TestEvent.class, handler);
        bus.unsubscribe(TestEvent.class, handler);
        bus.publish(new TestEvent("ignored"));
        assertTrue(received.isEmpty());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -pl . -Dtest="app.orgx.desktop.core.EventBusTest" -Dsurefire.failIfNoSpecifiedTests=false`
Expected: Compilation error — EventBus class does not exist

- [ ] **Step 3: Implement EventBus**

```java
package app.orgx.desktop.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {

    private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> handler) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public <T> void unsubscribe(Class<T> type, Consumer<T> handler) {
        var handlers = subscribers.get(type);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        var handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            for (var handler : handlers) {
                ((Consumer<T>) handler).accept(event);
            }
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -pl . -Dtest="app.orgx.desktop.core.EventBusTest"`
Expected: All 4 tests PASS

- [ ] **Step 5: Create all event records**

Each event is a simple Java record. Create these files in `src/main/java/app/orgx/desktop/core/events/`:

`VaultOpened.java`:
```java
package app.orgx.desktop.core.events;

import app.orgx.desktop.model.Vault;

public record VaultOpened(Vault vault) {}
```

`VaultClosed.java`:
```java
package app.orgx.desktop.core.events;

public record VaultClosed() {}
```

`NoteOpened.java`:
```java
package app.orgx.desktop.core.events;

import app.orgx.desktop.model.Note;

public record NoteOpened(Note note) {}
```

`NoteSaved.java`:
```java
package app.orgx.desktop.core.events;

import app.orgx.desktop.model.Note;

public record NoteSaved(Note note) {}
```

`NoteCreated.java`:
```java
package app.orgx.desktop.core.events;

import java.nio.file.Path;

public record NoteCreated(Path path) {}
```

`NoteDeleted.java`:
```java
package app.orgx.desktop.core.events;

import java.nio.file.Path;

public record NoteDeleted(Path path) {}
```

`NoteRenamed.java`:
```java
package app.orgx.desktop.core.events;

import java.nio.file.Path;

public record NoteRenamed(Path oldPath, Path newPath) {}
```

`NoteExternallyChanged.java`:
```java
package app.orgx.desktop.core.events;

import java.nio.file.Path;

public record NoteExternallyChanged(Path path) {}
```

`LinkClicked.java`:
```java
package app.orgx.desktop.core.events;

public record LinkClicked(String target) {}
```

`ThemeChanged.java`:
```java
package app.orgx.desktop.core.events;

public record ThemeChanged(String theme) {}
```

`SettingChanged.java`:
```java
package app.orgx.desktop.core.events;

public record SettingChanged(String key, Object value) {}
```

`CommandPaletteOpened.java`:
```java
package app.orgx.desktop.core.events;

public record CommandPaletteOpened() {}
```

`CommandPaletteClosed.java`:
```java
package app.orgx.desktop.core.events;

public record CommandPaletteClosed() {}
```

`SearchRequested.java`:
```java
package app.orgx.desktop.core.events;

public record SearchRequested(String query) {}
```

**Note:** The event records that reference `Note` and `Vault` (`VaultOpened`, `NoteOpened`, `NoteSaved`) will not compile until Task 3 creates the model classes. Create only the events that have no model dependencies in this step: `VaultClosed`, `NoteCreated`, `NoteDeleted`, `NoteRenamed`, `NoteExternallyChanged`, `LinkClicked`, `ThemeChanged`, `SettingChanged`, `CommandPaletteOpened`, `CommandPaletteClosed`, `SearchRequested`. The model-dependent events (`VaultOpened`, `NoteOpened`, `NoteSaved`) should be created in Task 3 after the model classes exist.

- [ ] **Step 6: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS — all events created in this step have no model dependencies

- [ ] **Step 7: Commit**

```bash
git add src/main/java/app/orgx/desktop/core/ src/test/java/app/orgx/desktop/core/
git commit -m "Add EventBus with pub/sub and all event records"
```

---

### Task 3: Data Model Records

**Files:**
- Create: `src/main/java/app/orgx/desktop/model/Note.java`
- Create: `src/main/java/app/orgx/desktop/model/Vault.java`
- Create: `src/main/java/app/orgx/desktop/model/Link.java`
- Create: `src/main/java/app/orgx/desktop/model/VaultEntry.java`
- Create: `src/main/java/app/orgx/desktop/model/VaultConfig.java`

- [ ] **Step 1: Create Note.java**

```java
package app.orgx.desktop.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

public class Note {

    private final Path path;
    private String title;
    private String content;
    private Instant lastModified;
    private Set<String> links;

    public Note(Path path, String title, String content, Instant lastModified, Set<String> links) {
        this.path = path;
        this.title = title;
        this.content = content;
        this.lastModified = lastModified;
        this.links = links;
    }

    public Path path() { return path; }
    public String title() { return title; }
    public String content() { return content; }
    public Instant lastModified() { return lastModified; }
    public Set<String> links() { return links; }

    public void setContent(String content) { this.content = content; }
    public void setTitle(String title) { this.title = title; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
    public void setLinks(Set<String> links) { this.links = links; }
}
```

Note is a mutable class (not a record) because content, links, and lastModified change as the user edits and the vault re-indexes.

- [ ] **Step 2: Create Vault.java**

```java
package app.orgx.desktop.model;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Vault {

    private final Path rootPath;
    private final String name;
    private final Map<String, Note> notes;

    public Vault(Path rootPath) {
        this.rootPath = rootPath;
        this.name = rootPath.getFileName().toString();
        this.notes = new ConcurrentHashMap<>();
    }

    public Path rootPath() { return rootPath; }
    public String name() { return name; }
    public Map<String, Note> notes() { return notes; }

    public Note getNote(String title) {
        return notes.get(title.toLowerCase());
    }

    public void putNote(Note note) {
        notes.put(note.title().toLowerCase(), note);
    }

    public void removeNote(String title) {
        notes.remove(title.toLowerCase());
    }
}
```

- [ ] **Step 3: Create Link.java**

```java
package app.orgx.desktop.model;

import java.nio.file.Path;

public record Link(Path source, String target, int lineNumber, String context) {}
```

- [ ] **Step 4: Create VaultEntry.java**

```java
package app.orgx.desktop.model;

import java.nio.file.Path;

public record VaultEntry(String name, Path path) {}
```

- [ ] **Step 5: Create VaultConfig.java**

```java
package app.orgx.desktop.model;

import java.nio.file.Path;
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
```

- [ ] **Step 6: Create model-dependent events deferred from Task 2**

Now that `Note` and `Vault` exist, create `VaultOpened.java`, `NoteOpened.java`, and `NoteSaved.java` in `src/main/java/app/orgx/desktop/core/events/` (code already provided in Task 2 Step 5).

- [ ] **Step 7: Verify full compilation including events**

Run: `./mvnw compile`
Expected: BUILD SUCCESS — events can now reference Note and Vault

- [ ] **Step 8: Run EventBus tests still pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.core.EventBusTest"`
Expected: All 4 tests PASS

- [ ] **Step 9: Commit**

```bash
git add src/main/java/app/orgx/desktop/model/ src/main/java/app/orgx/desktop/core/events/VaultOpened.java src/main/java/app/orgx/desktop/core/events/NoteOpened.java src/main/java/app/orgx/desktop/core/events/NoteSaved.java
git commit -m "Add data model classes and model-dependent event records"
```

---

### Task 4: ConfigManager

**Files:**
- Create: `src/main/java/app/orgx/desktop/config/ConfigManager.java`
- Create: `src/test/java/app/orgx/desktop/config/ConfigManagerTest.java`

- [ ] **Step 1: Write ConfigManager tests**

```java
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

    @TempDir
    Path tempDir;

    private ConfigManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConfigManager(tempDir.resolve("config.json"));
    }

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
        var nested = new ConfigManager(tempDir.resolve("a/b/config.json"));
        nested.save(new VaultConfig());
        var loaded = nested.load();
        assertNotNull(loaded);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.config.ConfigManagerTest"`
Expected: Compilation error — ConfigManager does not exist

- [ ] **Step 3: Implement ConfigManager**

Uses `java.util.Properties` for simplicity — flat key-value config without needing a JSON library. Vault list is stored as `vault.0.name=X`, `vault.0.path=Y`, etc.

```java
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

    public ConfigManager(Path configPath) {
        this.configPath = configPath;
    }

    public static ConfigManager createDefault() {
        var configDir = Path.of(System.getProperty("user.home"), ".config", "orgx");
        return new ConfigManager(configDir.resolve("config.properties"));
    }

    public VaultConfig load() {
        var config = new VaultConfig();
        if (!Files.exists(configPath)) {
            return config;
        }
        var props = new Properties();
        try (var reader = Files.newBufferedReader(configPath)) {
            props.load(reader);
        } catch (IOException e) {
            return config;
        }

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
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
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
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.config.ConfigManagerTest"`
Expected: All 3 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/config/ src/test/java/app/orgx/desktop/config/
git commit -m "Add ConfigManager with properties-based persistence"
```

---

### Task 5: NavigationHistory

**Files:**
- Create: `src/main/java/app/orgx/desktop/vault/NavigationHistory.java`
- Create: `src/test/java/app/orgx/desktop/vault/NavigationHistoryTest.java`

- [ ] **Step 1: Write NavigationHistory tests**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NavigationHistoryTest {

    private NavigationHistory history;

    @BeforeEach
    void setUp() {
        history = new NavigationHistory();
    }

    private Note note(String title) {
        return new Note(Path.of("/vault/" + title + ".md"), title, "", Instant.now(), Set.of());
    }

    @Test
    void initiallyCannotGoBackOrForward() {
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
    }

    @Test
    void afterPushingTwoNotesCanGoBack() {
        history.push(note("A"));
        history.push(note("B"));
        assertTrue(history.canGoBack());
        var back = history.back();
        assertEquals("A", back.title());
    }

    @Test
    void goingBackThenForwardRestoresNote() {
        history.push(note("A"));
        history.push(note("B"));
        history.back();
        assertTrue(history.canGoForward());
        var forward = history.forward();
        assertEquals("B", forward.title());
    }

    @Test
    void pushingAfterBackClearsForwardHistory() {
        history.push(note("A"));
        history.push(note("B"));
        history.back();
        history.push(note("C"));
        assertFalse(history.canGoForward());
    }

    @Test
    void backOnSingleEntryReturnsNull() {
        history.push(note("A"));
        assertFalse(history.canGoBack());
        assertNull(history.back());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.vault.NavigationHistoryTest"`
Expected: Compilation error — NavigationHistory does not exist

- [ ] **Step 3: Implement NavigationHistory**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NavigationHistory {

    private final List<Note> stack = new ArrayList<>();
    private int cursor = -1;

    public void push(Note note) {
        // Remove everything after cursor (discard forward history)
        while (stack.size() > cursor + 1) {
            stack.removeLast();
        }
        stack.add(note);
        cursor = stack.size() - 1;
    }

    public boolean canGoBack() {
        return cursor > 0;
    }

    public boolean canGoForward() {
        return cursor < stack.size() - 1;
    }

    public Note back() {
        if (!canGoBack()) return null;
        cursor--;
        return stack.get(cursor);
    }

    public Note forward() {
        if (!canGoForward()) return null;
        cursor++;
        return stack.get(cursor);
    }

    public void clear() {
        stack.clear();
        cursor = -1;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.vault.NavigationHistoryTest"`
Expected: All 5 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/vault/NavigationHistory.java src/test/java/app/orgx/desktop/vault/NavigationHistoryTest.java
git commit -m "Add NavigationHistory for back/forward note navigation"
```

---

### Task 6: WikiLink Extension for commonmark-java

**Files:**
- Create: `src/main/java/app/orgx/desktop/markdown/WikiLinkNode.java`
- Create: `src/main/java/app/orgx/desktop/markdown/WikiLinkExtension.java`
- Create: `src/test/java/app/orgx/desktop/markdown/WikiLinkExtensionTest.java`

- [ ] **Step 1: Write WikiLinkExtension tests**

```java
package app.orgx.desktop.markdown;

import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WikiLinkExtensionTest {

    private Parser parser;

    @BeforeEach
    void setUp() {
        parser = Parser.builder()
                .extensions(List.of(WikiLinkExtension.create()))
                .build();
    }

    @Test
    void parsesSimpleWikiLink() {
        var doc = parser.parse("Check [[My Note]] for details");
        var para = (Paragraph) doc.getFirstChild();
        // Text "Check " → WikiLinkNode "My Note" → Text " for details"
        var first = para.getFirstChild();
        assertInstanceOf(Text.class, first);

        var link = first.getNext();
        assertInstanceOf(WikiLinkNode.class, link);
        assertEquals("My Note", ((WikiLinkNode) link).getTarget());

        var last = link.getNext();
        assertInstanceOf(Text.class, last);
    }

    @Test
    void parsesMultipleWikiLinks() {
        var doc = parser.parse("See [[Note A]] and [[Note B]]");
        var para = (Paragraph) doc.getFirstChild();
        int wikiLinkCount = 0;
        var child = para.getFirstChild();
        while (child != null) {
            if (child instanceof WikiLinkNode) wikiLinkCount++;
            child = child.getNext();
        }
        assertEquals(2, wikiLinkCount);
    }

    @Test
    void doesNotParseIncompleteWikiLink() {
        var doc = parser.parse("This is [[not closed");
        var para = (Paragraph) doc.getFirstChild();
        var child = para.getFirstChild();
        while (child != null) {
            assertNotInstanceOf(WikiLinkNode.class, child);
            child = child.getNext();
        }
    }

    @Test
    void parsesWikiLinkAtStartOfLine() {
        var doc = parser.parse("[[First]] word");
        var para = (Paragraph) doc.getFirstChild();
        assertInstanceOf(WikiLinkNode.class, para.getFirstChild());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.markdown.WikiLinkExtensionTest"`
Expected: Compilation error — classes don't exist

- [ ] **Step 3: Create WikiLinkNode**

```java
package app.orgx.desktop.markdown;

import org.commonmark.node.CustomNode;

public class WikiLinkNode extends CustomNode {

    private final String target;

    public WikiLinkNode(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
```

- [ ] **Step 4: Create WikiLinkExtension**

Uses commonmark-java's `InlineContentParserFactory` to recognize `[[...]]` syntax.

```java
package app.orgx.desktop.markdown;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.Position;
import org.commonmark.parser.beta.ParsedInline;

import java.util.List;
import java.util.Set;

public class WikiLinkExtension implements Parser.ParserExtension {

    private WikiLinkExtension() {}

    public static Extension create() {
        return new WikiLinkExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.inlineContentParserFactory(new WikiLinkInlineParserFactory());
    }

    private static class WikiLinkInlineParserFactory implements InlineContentParserFactory {
        @Override
        public Set<Character> getTriggerCharacters() {
            return Set.of('[');
        }

        @Override
        public InlineContentParser create() {
            return new WikiLinkInlineParser();
        }
    }

    private static class WikiLinkInlineParser implements InlineContentParser {
        @Override
        public ParsedInline tryParse(InlineParserState state) {
            var scanner = state.scanner();

            // We're positioned at '['. Check for second '['
            scanner.next();
            if (scanner.peek() != '[') {
                return ParsedInline.none();
            }
            scanner.next();

            // Scan until ']]'
            var start = scanner.position();
            while (scanner.peek() != Scanner.END) {
                if (scanner.peek() == ']') {
                    var targetPos = scanner.position();
                    scanner.next();
                    if (scanner.peek() == ']') {
                        scanner.next();
                        var target = scanner.getSource(start, targetPos).toString();
                        if (!target.isEmpty()) {
                            return ParsedInline.of(new WikiLinkNode(target), scanner.position());
                        }
                        return ParsedInline.none();
                    }
                } else {
                    scanner.next();
                }
            }
            return ParsedInline.none();
        }
    }
}
```

Note: The exact `InlineContentParser` API depends on the commonmark-java version. The beta inline parser API (`org.commonmark.parser.beta`) was introduced around 0.21+. If the API differs, consult the commonmark-java source/javadoc for the correct approach. An alternative is to use a `PostProcessor` that does regex replacement on `Text` nodes. If the beta API causes issues, switch to this simpler approach:

**Fallback PostProcessor approach:**
```java
@Override
public void extend(Parser.Builder parserBuilder) {
    parserBuilder.postProcessor(new WikiLinkPostProcessor());
}
```
Where `WikiLinkPostProcessor` walks all `Text` nodes, finds `[[...]]` patterns via regex, and replaces them with `Text` + `WikiLinkNode` + `Text` sequences.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.markdown.WikiLinkExtensionTest"`
Expected: All 4 tests PASS

If the beta inline parser API doesn't match, adjust the implementation to use the PostProcessor approach and re-run.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/app/orgx/desktop/markdown/ src/test/java/app/orgx/desktop/markdown/
git commit -m "Add WikiLink commonmark extension for [[link]] parsing"
```

---

### Task 7: VaultManager — Scanning & Note CRUD

**Files:**
- Create: `src/main/java/app/orgx/desktop/vault/VaultManager.java`
- Create: `src/test/java/app/orgx/desktop/vault/VaultManagerTest.java`

- [ ] **Step 1: Write VaultManager tests**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteCreated;
import app.orgx.desktop.core.events.NoteDeleted;
import app.orgx.desktop.core.events.VaultOpened;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class VaultManagerTest {

    @TempDir
    Path vaultDir;

    private EventBus bus;
    private VaultManager manager;

    @BeforeEach
    void setUp() {
        bus = new EventBus();
        manager = new VaultManager(bus);
    }

    private void createFile(String name, String content) throws IOException {
        Files.writeString(vaultDir.resolve(name), content);
    }

    @Test
    void openVaultScansMarkdownFiles() throws IOException {
        createFile("note1.md", "# Note 1\nSome text");
        createFile("note2.md", "# Note 2\nMore text");
        createFile("readme.txt", "not a note");

        manager.openVault(vaultDir);

        var vault = manager.getVault();
        assertNotNull(vault);
        assertEquals(2, vault.notes().size());
        assertNotNull(vault.getNote("note1"));
        assertNotNull(vault.getNote("note2"));
    }

    @Test
    void openVaultFiresVaultOpenedEvent() throws IOException {
        createFile("test.md", "hello");
        var events = new ArrayList<VaultOpened>();
        bus.subscribe(VaultOpened.class, events::add);

        manager.openVault(vaultDir);

        assertEquals(1, events.size());
        assertEquals(vaultDir, events.getFirst().vault().rootPath());
    }

    @Test
    void openVaultScansSubdirectories() throws IOException {
        Files.createDirectories(vaultDir.resolve("sub"));
        createFile("sub/nested.md", "nested note");

        manager.openVault(vaultDir);

        assertNotNull(manager.getVault().getNote("nested"));
    }

    @Test
    void createNoteCreatesFileAndAddsToVault() throws IOException {
        manager.openVault(vaultDir);

        var events = new ArrayList<NoteCreated>();
        bus.subscribe(NoteCreated.class, events::add);

        var note = manager.createNote("New Note");

        assertTrue(Files.exists(vaultDir.resolve("New Note.md")));
        assertNotNull(manager.getVault().getNote("New Note"));
        assertEquals(1, events.size());
    }

    @Test
    void deleteNoteRemovesFileAndFromVault() throws IOException {
        createFile("doomed.md", "goodbye");
        manager.openVault(vaultDir);

        var events = new ArrayList<NoteDeleted>();
        bus.subscribe(NoteDeleted.class, events::add);

        manager.deleteNote(vaultDir.resolve("doomed.md"));

        assertFalse(Files.exists(vaultDir.resolve("doomed.md")));
        assertNull(manager.getVault().getNote("doomed"));
        assertEquals(1, events.size());
    }

    @Test
    void getNoteBytitleIsCaseInsensitive() throws IOException {
        createFile("My Note.md", "content");
        manager.openVault(vaultDir);

        assertNotNull(manager.getVault().getNote("my note"));
        assertNotNull(manager.getVault().getNote("MY NOTE"));
    }

    @Test
    void noteTitleIsFilenameWithoutExtension() throws IOException {
        createFile("Hello World.md", "content");
        manager.openVault(vaultDir);

        var note = manager.getVault().getNote("Hello World");
        assertEquals("Hello World", note.title());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.vault.VaultManagerTest"`
Expected: Compilation error — VaultManager does not exist

- [ ] **Step 3: Implement VaultManager**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.*;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VaultManager {

    private static final Pattern WIKILINK_PATTERN = Pattern.compile("\\[\\[([^\\]]+)]]");

    private final EventBus eventBus;
    private Vault vault;

    public VaultManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void openVault(Path rootPath) throws IOException {
        vault = new Vault(rootPath);
        scanVault(rootPath);
        eventBus.publish(new VaultOpened(vault));
    }

    public void closeVault() {
        vault = null;
        eventBus.publish(new VaultClosed());
    }

    public Vault getVault() {
        return vault;
    }

    public Note createNote(String title) throws IOException {
        if (vault == null) throw new IllegalStateException("No vault open");
        var path = vault.rootPath().resolve(title + ".md");
        Files.writeString(path, "");
        var note = new Note(path, title, "", Instant.now(), Set.of());
        vault.putNote(note);
        eventBus.publish(new NoteCreated(path));
        return note;
    }

    public void deleteNote(Path path) throws IOException {
        if (vault == null) throw new IllegalStateException("No vault open");
        var title = fileNameToTitle(path);
        Files.deleteIfExists(path);
        vault.removeNote(title);
        eventBus.publish(new NoteDeleted(path));
    }

    public void renameNote(Path oldPath, String newTitle) throws IOException {
        if (vault == null) throw new IllegalStateException("No vault open");
        var oldTitle = fileNameToTitle(oldPath);
        var newPath = oldPath.resolveSibling(newTitle + ".md");
        Files.move(oldPath, newPath);
        vault.removeNote(oldTitle);

        var content = Files.readString(newPath);
        var note = new Note(newPath, newTitle, content,
                Files.getLastModifiedTime(newPath).toInstant(), extractLinks(content));
        vault.putNote(note);
        eventBus.publish(new NoteRenamed(oldPath, newPath));
    }

    public Note loadNote(Path path) throws IOException {
        var content = Files.readString(path);
        var title = fileNameToTitle(path);
        var note = vault.getNote(title);
        if (note != null) {
            note.setContent(content);
            note.setLastModified(Files.getLastModifiedTime(path).toInstant());
            note.setLinks(extractLinks(content));
        }
        return note;
    }

    private void scanVault(Path root) throws IOException {
        try (var walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".md"))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        var content = Files.readString(p);
                        var title = fileNameToTitle(p);
                        var note = new Note(p, title, content,
                                Files.getLastModifiedTime(p).toInstant(),
                                extractLinks(content));
                        vault.putNote(note);
                    } catch (IOException e) {
                        // Skip unreadable files
                    }
                });
        }
    }

    public static String fileNameToTitle(Path path) {
        var name = path.getFileName().toString();
        return name.endsWith(".md") ? name.substring(0, name.length() - 3) : name;
    }

    public static Set<String> extractLinks(String content) {
        var matcher = WIKILINK_PATTERN.matcher(content);
        return matcher.results()
                .map(r -> r.group(1))
                .collect(Collectors.toSet());
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.vault.VaultManagerTest"`
Expected: All 7 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/vault/VaultManager.java src/test/java/app/orgx/desktop/vault/VaultManagerTest.java
git commit -m "Add VaultManager with vault scanning and note CRUD"
```

---

### Task 8: LinkIndex — Backlink Graph

**Files:**
- Create: `src/main/java/app/orgx/desktop/vault/LinkIndex.java`
- Create: `src/test/java/app/orgx/desktop/vault/LinkIndexTest.java`

- [ ] **Step 1: Write LinkIndex tests**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.model.Link;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LinkIndexTest {

    private LinkIndex index;
    private Vault vault;

    @BeforeEach
    void setUp() {
        index = new LinkIndex();
        vault = new Vault(Path.of("/vault"));
    }

    private Note note(String title, String content, Set<String> links) {
        var n = new Note(Path.of("/vault/" + title + ".md"), title, content, Instant.now(), links);
        vault.putNote(n);
        return n;
    }

    @Test
    void backlinkIsFoundAfterRebuild() {
        var noteA = note("A", "See [[B]] here", Set.of("B"));
        var noteB = note("B", "Just content", Set.of());

        index.rebuild(vault);

        var backlinks = index.getBacklinks(noteB);
        assertEquals(1, backlinks.size());
        assertEquals("A", backlinks.getFirst().source().getFileName().toString().replace(".md", ""));
    }

    @Test
    void noBacklinksForUnlinkedNote() {
        note("A", "no links", Set.of());
        note("B", "also no links", Set.of());

        index.rebuild(vault);

        var backlinks = index.getBacklinks(vault.getNote("A"));
        assertTrue(backlinks.isEmpty());
    }

    @Test
    void multipleBacklinks() {
        note("A", "link to [[C]]", Set.of("C"));
        note("B", "also links to [[C]]", Set.of("C"));
        var noteC = note("C", "popular note", Set.of());

        index.rebuild(vault);

        var backlinks = index.getBacklinks(noteC);
        assertEquals(2, backlinks.size());
    }

    @Test
    void updateNoteRefreshesLinks() {
        var noteA = note("A", "link to [[B]]", Set.of("B"));
        var noteB = note("B", "content", Set.of());
        index.rebuild(vault);

        // A no longer links to B
        noteA.setContent("no more links");
        noteA.setLinks(Set.of());
        index.updateNote(noteA);

        var backlinks = index.getBacklinks(noteB);
        assertTrue(backlinks.isEmpty());
    }

    @Test
    void backlinkContextContainsLinkLine() {
        note("A", "Line one\nSee [[B]] for info\nLine three", Set.of("B"));
        var noteB = note("B", "content", Set.of());

        index.rebuild(vault);

        var backlinks = index.getBacklinks(noteB);
        assertEquals(1, backlinks.size());
        assertTrue(backlinks.getFirst().context().contains("[[B]]"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.vault.LinkIndexTest"`
Expected: Compilation error — LinkIndex does not exist

- [ ] **Step 3: Implement LinkIndex**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.model.Link;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;

import java.util.*;
import java.util.regex.Pattern;

public class LinkIndex {

    private static final Pattern WIKILINK_PATTERN = Pattern.compile("\\[\\[([^\\]]+)]]");

    // target title (lowercase) → list of Links pointing to it
    private final Map<String, List<Link>> backlinks = new HashMap<>();

    public void rebuild(Vault vault) {
        backlinks.clear();
        for (var note : vault.notes().values()) {
            indexNote(note);
        }
    }

    public void updateNote(Note note) {
        // Remove old backlinks from this source
        backlinks.values().forEach(links ->
                links.removeIf(link -> link.source().equals(note.path())));

        // Re-index
        indexNote(note);
    }

    public void removeNote(Note note) {
        backlinks.values().forEach(links ->
                links.removeIf(link -> link.source().equals(note.path())));
        backlinks.remove(note.title().toLowerCase());
    }

    public List<Link> getBacklinks(Note note) {
        return backlinks.getOrDefault(note.title().toLowerCase(), List.of());
    }

    public List<Link> getOutgoingLinks(Note note) {
        var result = new ArrayList<Link>();
        var lines = note.content().split("\n");
        for (int i = 0; i < lines.length; i++) {
            var matcher = WIKILINK_PATTERN.matcher(lines[i]);
            while (matcher.find()) {
                result.add(new Link(note.path(), matcher.group(1), i + 1, lines[i]));
            }
        }
        return result;
    }

    private void indexNote(Note note) {
        var lines = note.content().split("\n");
        for (int i = 0; i < lines.length; i++) {
            var matcher = WIKILINK_PATTERN.matcher(lines[i]);
            while (matcher.find()) {
                var target = matcher.group(1).toLowerCase();
                var link = new Link(note.path(), matcher.group(1), i + 1, lines[i]);
                backlinks.computeIfAbsent(target, k -> new ArrayList<>()).add(link);
            }
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.vault.LinkIndexTest"`
Expected: All 5 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/vault/LinkIndex.java src/test/java/app/orgx/desktop/vault/LinkIndexTest.java
git commit -m "Add LinkIndex for wikilink backlink graph"
```

---

### Task 9: SearchEngine — Lucene Integration

**Files:**
- Create: `src/main/java/app/orgx/desktop/search/SearchResult.java`
- Create: `src/main/java/app/orgx/desktop/search/SearchEngine.java`
- Create: `src/test/java/app/orgx/desktop/search/SearchEngineTest.java`

- [ ] **Step 1: Create SearchResult**

```java
package app.orgx.desktop.search;

import java.nio.file.Path;

public record SearchResult(Path path, String title, String snippet, float score) {}
```

- [ ] **Step 2: Write SearchEngine tests**

```java
package app.orgx.desktop.search;

import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {

    private SearchEngine engine;
    private Vault vault;

    @BeforeEach
    void setUp() throws Exception {
        engine = new SearchEngine();
        vault = new Vault(Path.of("/vault"));
    }

    @AfterEach
    void tearDown() throws Exception {
        engine.close();
    }

    private void addNote(String title, String content) {
        vault.putNote(new Note(
                Path.of("/vault/" + title + ".md"), title, content, Instant.now(), Set.of()));
    }

    @Test
    void searchByTitleFindsNote() throws Exception {
        addNote("Meeting Notes", "Some meeting content");
        addNote("Shopping List", "Milk, eggs");
        engine.rebuildIndex(vault);

        var results = engine.searchByTitle("meeting", 10);
        assertEquals(1, results.size());
        assertEquals("Meeting Notes", results.getFirst().title());
    }

    @Test
    void searchContentFindsMatchingNotes() throws Exception {
        addNote("Note A", "The deployment pipeline needs fixing");
        addNote("Note B", "Shopping list for today");
        engine.rebuildIndex(vault);

        var results = engine.searchContent("deployment pipeline", 10);
        assertEquals(1, results.size());
        assertEquals("Note A", results.getFirst().title());
    }

    @Test
    void searchContentReturnsSnippet() throws Exception {
        addNote("Note A", "First line\nThe deployment pipeline needs fixing\nLast line");
        engine.rebuildIndex(vault);

        var results = engine.searchContent("deployment", 10);
        assertFalse(results.isEmpty());
        assertNotNull(results.getFirst().snippet());
        // Snippet should contain the matching term
        assertTrue(results.getFirst().snippet().toLowerCase().contains("deployment"));
    }

    @Test
    void fuzzyTitleSearchToleratesTypos() throws Exception {
        addNote("Obsidian", "Note about obsidian");
        engine.rebuildIndex(vault);

        var results = engine.searchByTitle("obsidan", 10);  // typo
        assertFalse(results.isEmpty());
        assertEquals("Obsidian", results.getFirst().title());
    }

    @Test
    void incrementalUpdateIndexesSingleNote() throws Exception {
        addNote("Original", "original content");
        engine.rebuildIndex(vault);

        var newNote = new Note(Path.of("/vault/New.md"), "New", "brand new note", Instant.now(), Set.of());
        engine.updateNote(newNote);

        var results = engine.searchContent("brand new", 10);
        assertEquals(1, results.size());
        assertEquals("New", results.getFirst().title());
    }

    @Test
    void deleteNoteRemovesFromIndex() throws Exception {
        addNote("ToDelete", "delete me");
        engine.rebuildIndex(vault);

        engine.deleteNote(Path.of("/vault/ToDelete.md"));

        var results = engine.searchContent("delete me", 10);
        assertTrue(results.isEmpty());
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.search.SearchEngineTest"`
Expected: Compilation error — SearchEngine does not exist

- [ ] **Step 4: Implement SearchEngine**

```java
package app.orgx.desktop.search;

import app.orgx.desktop.model.Note;
import app.orgx.desktop.model.Vault;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine implements Closeable {

    private final ByteBuffersDirectory directory;
    private final StandardAnalyzer analyzer;
    private IndexWriter writer;

    public SearchEngine() throws IOException {
        this.directory = new ByteBuffersDirectory();
        this.analyzer = new StandardAnalyzer();
        this.writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));
    }

    public void rebuildIndex(Vault vault) throws IOException {
        writer.deleteAll();
        for (var note : vault.notes().values()) {
            writer.addDocument(toDocument(note));
        }
        writer.commit();
    }

    public void updateNote(Note note) throws IOException {
        writer.updateDocument(new Term("path", note.path().toString()), toDocument(note));
        writer.commit();
    }

    public void deleteNote(Path path) throws IOException {
        writer.deleteDocuments(new Term("path", path.toString()));
        writer.commit();
    }

    public List<SearchResult> searchByTitle(String query, int maxResults) throws Exception {
        try (var reader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(reader);

            // Fuzzy query on title for typo tolerance
            var fuzzyQuery = new FuzzyQuery(new Term("title", query.toLowerCase()), 2);

            // Also try prefix query for partial matches
            var prefixQuery = new PrefixQuery(new Term("title", query.toLowerCase()));

            // Combine with OR
            var combined = new BooleanQuery.Builder()
                    .add(fuzzyQuery, BooleanClause.Occur.SHOULD)
                    .add(prefixQuery, BooleanClause.Occur.SHOULD)
                    .build();

            var topDocs = searcher.search(combined, maxResults);
            return toResults(searcher, topDocs, null);
        }
    }

    public List<SearchResult> searchContent(String queryStr, int maxResults) throws Exception {
        try (var reader = DirectoryReader.open(directory)) {
            var searcher = new IndexSearcher(reader);
            var queryParser = new QueryParser("content", analyzer);
            var query = queryParser.parse(queryStr);

            var topDocs = searcher.search(query, maxResults);

            // Highlight matching snippets
            var highlighter = UnifiedHighlighter.builder(searcher, analyzer).build();
            var snippets = highlighter.highlight("content", query, topDocs);

            return toResults(searcher, topDocs, snippets);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
        directory.close();
    }

    private Document toDocument(Note note) {
        var doc = new Document();
        doc.add(new StringField("path", note.path().toString(), Field.Store.YES));
        doc.add(new TextField("title", note.title().toLowerCase(), Field.Store.YES));
        doc.add(new StoredField("displayTitle", note.title()));
        doc.add(new TextField("content", note.content(), Field.Store.YES));
        doc.add(new LongField("modified", note.lastModified().toEpochMilli()));
        return doc;
    }

    private List<SearchResult> toResults(IndexSearcher searcher, TopDocs topDocs, String[] snippets) throws IOException {
        var results = new ArrayList<SearchResult>();
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            var doc = searcher.storedFields().document(topDocs.scoreDocs[i].doc);
            var path = Path.of(doc.get("path"));
            var title = doc.get("displayTitle");
            var snippet = snippets != null && i < snippets.length ? snippets[i] : "";
            var score = topDocs.scoreDocs[i].score;
            results.add(new SearchResult(path, title, snippet, score));
        }
        return results;
    }
}
```

Note: The Lucene 10.x API may have changed from 9.x. Key differences to watch for:
- `UnifiedHighlighter` construction may use a builder pattern
- `LongField` / `LongPoint` naming may differ
- `StoredField` vs `Field.Store.YES` usage
- Check Lucene 10.x Javadoc if compilation fails and adjust field types accordingly

- [ ] **Step 5: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.search.SearchEngineTest"`
Expected: All 6 tests PASS

If Lucene API mismatches cause compilation errors, consult the Lucene 10.x Javadoc/migration guide and adjust. The test expectations remain the same.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/app/orgx/desktop/search/ src/test/java/app/orgx/desktop/search/
git commit -m "Add Lucene-backed SearchEngine with fuzzy title and content search"
```

---

### Task 10: FileWatcher

**Files:**
- Create: `src/main/java/app/orgx/desktop/vault/FileWatcher.java`

FileWatcher is hard to unit test reliably (filesystem timing, OS-specific behavior). We test it manually and through integration later.

- [ ] **Step 1: Implement FileWatcher**

```java
package app.orgx.desktop.vault;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteExternallyChanged;
import app.orgx.desktop.core.events.NoteCreated;
import app.orgx.desktop.core.events.NoteDeleted;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path rootPath;
    private final EventBus eventBus;
    private volatile boolean running = true;
    private WatchService watchService;

    public FileWatcher(Path rootPath, EventBus eventBus) {
        this.rootPath = rootPath;
        this.eventBus = eventBus;
    }

    public void start() {
        var thread = new Thread(this, "file-watcher");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            registerTree(rootPath);

            while (running) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (ClosedWatchServiceException | InterruptedException e) {
                    break;
                }

                var dir = (Path) key.watchable();
                for (var event : key.pollEvents()) {
                    var kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    var path = dir.resolve(((WatchEvent<Path>) event).context());

                    if (!path.toString().endsWith(".md")) continue;

                    if (kind == ENTRY_CREATE) {
                        eventBus.publish(new NoteCreated(path));
                    } else if (kind == ENTRY_DELETE) {
                        eventBus.publish(new NoteDeleted(path));
                    } else if (kind == ENTRY_MODIFY) {
                        eventBus.publish(new NoteExternallyChanged(path));
                    }
                }

                if (!key.reset()) break;
            }
        } catch (IOException e) {
            // Log and stop
        }
    }

    private void registerTree(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/app/orgx/desktop/vault/FileWatcher.java
git commit -m "Add FileWatcher for detecting external vault changes"
```

---

### Task 11: Light & Dark Theme CSS

**Files:**
- Create: `src/main/resources/themes/light.css`
- Create: `src/main/resources/themes/dark.css`

- [ ] **Step 1: Create light.css**

```css
/* Light theme for Orgx */

.root {
    -fx-base: #ffffff;
    -fx-background: #f5f5f5;
    -fx-font-family: "System";
    -fx-font-size: 14px;
}

/* File tree */
.file-tree {
    -fx-background-color: #f0f0f0;
}

.file-tree .tree-cell:selected {
    -fx-background-color: #d0d0d0;
    -fx-text-fill: #1a1a1a;
}

/* Editor */
.note-editor {
    -fx-background-color: #ffffff;
}

/* Backlinks panel */
.backlinks-panel {
    -fx-background-color: #f0f0f0;
}

.backlink-card {
    -fx-background-color: #ffffff;
    -fx-border-color: #e0e0e0;
    -fx-border-radius: 4;
    -fx-background-radius: 4;
    -fx-padding: 8;
}

.backlink-card:hover {
    -fx-background-color: #f0f8ff;
    -fx-border-color: #b0b0b0;
    -fx-cursor: hand;
}

.backlink-title {
    -fx-font-weight: bold;
    -fx-text-fill: #1a1a1a;
}

.backlink-context {
    -fx-text-fill: #666666;
    -fx-font-size: 12px;
}

/* Status bar */
.status-bar {
    -fx-background-color: #e8e8e8;
    -fx-padding: 2 8;
    -fx-font-size: 12px;
}

.status-bar .label {
    -fx-text-fill: #666666;
}

/* Command palette */
.command-palette {
    -fx-background-color: #ffffff;
    -fx-border-color: #cccccc;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 16, 0, 0, 4);
}

.command-palette .text-field {
    -fx-background-color: transparent;
    -fx-font-size: 16px;
    -fx-padding: 12;
    -fx-border-width: 0 0 1 0;
    -fx-border-color: #e0e0e0;
}

.command-palette .list-view {
    -fx-background-color: transparent;
}

.command-palette .list-cell:selected {
    -fx-background-color: #e8f0fe;
    -fx-text-fill: #1a1a1a;
}

.palette-result-title {
    -fx-font-size: 14px;
    -fx-text-fill: #1a1a1a;
}

.palette-result-subtitle {
    -fx-font-size: 12px;
    -fx-text-fill: #888888;
}

/* Wikilink styling */
.wikilink {
    -fx-fill: #4a86c8;
    -fx-cursor: hand;
}

/* Markdown heading sizes */
.heading-1 { -fx-font-size: 28px; -fx-font-weight: bold; }
.heading-2 { -fx-font-size: 24px; -fx-font-weight: bold; }
.heading-3 { -fx-font-size: 20px; -fx-font-weight: bold; }
.heading-4 { -fx-font-size: 18px; -fx-font-weight: bold; }
.heading-5 { -fx-font-size: 16px; -fx-font-weight: bold; }
.heading-6 { -fx-font-size: 14px; -fx-font-weight: bold; }

/* Code */
.code-inline {
    -fx-font-family: "Monospace";
    -fx-background-color: #f0f0f0;
    -fx-padding: 1 4;
}

.code-block {
    -fx-font-family: "Monospace";
    -fx-background-color: #f5f5f5;
    -fx-padding: 8;
}

/* Blockquote */
.blockquote {
    -fx-border-color: #cccccc;
    -fx-border-width: 0 0 0 3;
    -fx-padding: 4 8 4 12;
}

/* Syntax markers (hidden in WYSIWYG mode) */
.syntax-hidden {
    -fx-font-size: 0;
    -fx-opacity: 0;
}

.syntax-visible {
    -fx-text-fill: #aaaaaa;
}

/* Split pane dividers */
.split-pane > .split-pane-divider {
    -fx-background-color: #e0e0e0;
    -fx-padding: 0 1 0 1;
}
```

- [ ] **Step 2: Create dark.css**

```css
/* Dark theme for Orgx */

.root {
    -fx-base: #1e1e1e;
    -fx-background: #1e1e1e;
    -fx-font-family: "System";
    -fx-font-size: 14px;
    -fx-text-fill: #d4d4d4;
}

/* File tree */
.file-tree {
    -fx-background-color: #252526;
}

.file-tree .tree-cell:selected {
    -fx-background-color: #37373d;
    -fx-text-fill: #e0e0e0;
}

/* Editor */
.note-editor {
    -fx-background-color: #1e1e1e;
}

/* Backlinks panel */
.backlinks-panel {
    -fx-background-color: #252526;
}

.backlink-card {
    -fx-background-color: #2d2d2d;
    -fx-border-color: #3e3e42;
    -fx-border-radius: 4;
    -fx-background-radius: 4;
    -fx-padding: 8;
}

.backlink-card:hover {
    -fx-background-color: #37373d;
    -fx-border-color: #4e4e52;
    -fx-cursor: hand;
}

.backlink-title {
    -fx-font-weight: bold;
    -fx-text-fill: #e0e0e0;
}

.backlink-context {
    -fx-text-fill: #999999;
    -fx-font-size: 12px;
}

/* Status bar */
.status-bar {
    -fx-background-color: #007acc;
    -fx-padding: 2 8;
    -fx-font-size: 12px;
}

.status-bar .label {
    -fx-text-fill: #ffffff;
}

/* Command palette */
.command-palette {
    -fx-background-color: #252526;
    -fx-border-color: #3e3e42;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 16, 0, 0, 4);
}

.command-palette .text-field {
    -fx-background-color: transparent;
    -fx-text-fill: #e0e0e0;
    -fx-font-size: 16px;
    -fx-padding: 12;
    -fx-border-width: 0 0 1 0;
    -fx-border-color: #3e3e42;
}

.command-palette .list-view {
    -fx-background-color: transparent;
}

.command-palette .list-cell:selected {
    -fx-background-color: #094771;
    -fx-text-fill: #e0e0e0;
}

.palette-result-title {
    -fx-font-size: 14px;
    -fx-text-fill: #e0e0e0;
}

.palette-result-subtitle {
    -fx-font-size: 12px;
    -fx-text-fill: #808080;
}

/* Wikilink styling */
.wikilink {
    -fx-fill: #569cd6;
    -fx-cursor: hand;
}

/* Markdown heading sizes */
.heading-1 { -fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0; }
.heading-2 { -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0; }
.heading-3 { -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0; }
.heading-4 { -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0; }
.heading-5 { -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0; }
.heading-6 { -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0; }

/* Code */
.code-inline {
    -fx-font-family: "Monospace";
    -fx-background-color: #2d2d2d;
    -fx-padding: 1 4;
}

.code-block {
    -fx-font-family: "Monospace";
    -fx-background-color: #1e1e1e;
    -fx-padding: 8;
}

/* Blockquote */
.blockquote {
    -fx-border-color: #4e4e52;
    -fx-border-width: 0 0 0 3;
    -fx-padding: 4 8 4 12;
}

/* Syntax markers */
.syntax-hidden {
    -fx-font-size: 0;
    -fx-opacity: 0;
}

.syntax-visible {
    -fx-text-fill: #555555;
}

/* Split pane dividers */
.split-pane > .split-pane-divider {
    -fx-background-color: #3e3e42;
    -fx-padding: 0 1 0 1;
}
```

- [ ] **Step 3: Verify resources are on classpath**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/themes/
git commit -m "Add light and dark theme CSS stylesheets"
```

---

### Task 12: UI Shell — AppShell & StatusBar

**Files:**
- Create: `src/main/java/app/orgx/desktop/ui/AppShell.java`
- Create: `src/main/java/app/orgx/desktop/ui/StatusBar.java`
- Modify: `src/main/java/app/orgx/desktop/OrgxApplication.java`

This task creates the main layout skeleton. Panels are placeholder `Pane`s for now — populated in later tasks.

- [ ] **Step 1: Create StatusBar**

```java
package app.orgx.desktop.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class StatusBar extends HBox {

    private final Label pathLabel = new Label();
    private final Label wordCountLabel = new Label();
    private final Label cursorLabel = new Label();

    public StatusBar() {
        getStyleClass().add("status-bar");
        setPadding(new Insets(2, 8, 2, 8));
        setSpacing(16);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(pathLabel, spacer, wordCountLabel, cursorLabel);
    }

    public void setPath(String path) {
        pathLabel.setText(path);
    }

    public void setWordCount(int count) {
        wordCountLabel.setText("Words: " + count);
    }

    public void setCursorPosition(int line, int col) {
        cursorLabel.setText("Ln " + line + ", Col " + col);
    }
}
```

- [ ] **Step 2: Create AppShell**

```java
package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class AppShell extends BorderPane {

    private final SplitPane splitPane;
    private final StackPane centerStack;
    private final StatusBar statusBar;

    // Placeholders — replaced by actual panels in later tasks
    private Pane fileTreePanel = new Pane();
    private Pane editorPanel = new Pane();
    private Pane backlinksPanel = new Pane();

    private boolean fileTreeVisible = true;
    private boolean backlinksVisible = true;

    public AppShell(EventBus eventBus) {
        splitPane = new SplitPane(fileTreePanel, editorPanel, backlinksPanel);
        splitPane.setDividerPositions(0.2, 0.75);

        centerStack = new StackPane(splitPane);
        setCenter(centerStack);

        statusBar = new StatusBar();
        // Hidden by default per spec
    }

    public void setFileTreePanel(Pane panel) {
        var idx = splitPane.getItems().indexOf(fileTreePanel);
        if (idx >= 0) splitPane.getItems().set(idx, panel);
        this.fileTreePanel = panel;
    }

    public void setEditorPanel(Pane panel) {
        var idx = splitPane.getItems().indexOf(editorPanel);
        if (idx >= 0) splitPane.getItems().set(idx, panel);
        this.editorPanel = panel;
    }

    public void setBacklinksPanel(Pane panel) {
        var idx = splitPane.getItems().indexOf(backlinksPanel);
        if (idx >= 0) splitPane.getItems().set(idx, panel);
        this.backlinksPanel = panel;
    }

    public StackPane getCenterStack() {
        return centerStack;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setStatusBarVisible(boolean visible) {
        if (visible) {
            setBottom(statusBar);
        } else {
            setBottom(null);
        }
    }

    public void toggleFileTree() {
        fileTreeVisible = !fileTreeVisible;
        if (fileTreeVisible) {
            if (!splitPane.getItems().contains(fileTreePanel)) {
                splitPane.getItems().addFirst(fileTreePanel);
            }
        } else {
            splitPane.getItems().remove(fileTreePanel);
        }
    }

    public void toggleBacklinks() {
        backlinksVisible = !backlinksVisible;
        if (backlinksVisible) {
            if (!splitPane.getItems().contains(backlinksPanel)) {
                splitPane.getItems().addLast(backlinksPanel);
            }
        } else {
            splitPane.getItems().remove(backlinksPanel);
        }
    }

    public boolean isFileTreeVisible() { return fileTreeVisible; }
    public boolean isBacklinksVisible() { return backlinksVisible; }
}
```

- [ ] **Step 3: Update OrgxApplication to use AppShell**

Replace the entire `OrgxApplication.java` with:

```java
package app.orgx.desktop;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrgxApplication extends Application {

    private static final Logger log = LogManager.getLogger(OrgxApplication.class);

    @Override
    public void start(Stage stage) {
        log.info("Starting application");

        var eventBus = new EventBus();
        var shell = new AppShell(eventBus);

        var scene = new Scene(shell, 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/themes/light.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Orgx");
        stage.show();
    }
}
```

- [ ] **Step 4: Verify it compiles and runs**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

Run: `./mvnw javafx:run` (manual smoke test — should show a blank window with three empty split panes)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/ui/AppShell.java src/main/java/app/orgx/desktop/ui/StatusBar.java src/main/java/app/orgx/desktop/OrgxApplication.java
git commit -m "Add AppShell layout skeleton with SplitPane and StatusBar"
```

---

### Task 13: FileTreePanel

**Files:**
- Create: `src/main/java/app/orgx/desktop/ui/FileTreePanel.java`

- [ ] **Step 1: Implement FileTreePanel**

```java
package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.*;
import app.orgx.desktop.model.Vault;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileTreePanel extends VBox {

    private final TreeView<Path> treeView;
    private final EventBus eventBus;
    private final app.orgx.desktop.vault.VaultManager vaultManager;
    private Vault vault;

    public FileTreePanel(EventBus eventBus, app.orgx.desktop.vault.VaultManager vaultManager) {
        this.eventBus = eventBus;
        this.vaultManager = vaultManager;
        this.treeView = new TreeView<>();
        getStyleClass().add("file-tree");

        treeView.setShowRoot(true);
        treeView.setCellFactory(tv -> new FileTreeCell());

        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                var selected = treeView.getSelectionModel().getSelectedItem();
                if (selected != null && Files.isRegularFile(selected.getValue())) {
                    eventBus.publish(new NoteOpened(
                            vault.getNote(fileToTitle(selected.getValue()))));
                }
            }
        });

        getChildren().add(treeView);
        treeView.prefHeightProperty().bind(heightProperty());

        setupContextMenu();
        subscribeToEvents();
    }

    public void loadVault(Vault vault) {
        this.vault = vault;
        var root = createTreeItem(vault.rootPath());
        root.setExpanded(true);
        treeView.setRoot(root);
    }

    private TreeItem<Path> createTreeItem(Path dir) {
        var item = new TreeItem<>(dir);
        try (var stream = Files.list(dir)) {
            stream.sorted(Comparator
                    .<Path, Boolean>comparing(p -> !Files.isDirectory(p))
                    .thenComparing(p -> p.getFileName().toString().toLowerCase()))
                .forEach(p -> {
                    if (Files.isDirectory(p)) {
                        item.getChildren().add(createTreeItem(p));
                    } else if (p.toString().endsWith(".md")) {
                        item.getChildren().add(new TreeItem<>(p));
                    }
                });
        } catch (IOException ignored) {}
        return item;
    }

    private void setupContextMenu() {
        var newNote = new MenuItem("New Note");
        newNote.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var dir = Files.isDirectory(selected.getValue())
                    ? selected.getValue()
                    : selected.getValue().getParent();
            var dialog = new TextInputDialog("Untitled");
            dialog.setTitle("New Note");
            dialog.setHeaderText("Enter note name:");
            dialog.showAndWait().ifPresent(name -> {
                try {
                    vaultManager.createNote(name);
                } catch (IOException ignored) {}
            });
        });

        var newFolder = new MenuItem("New Folder");
        newFolder.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var dir = Files.isDirectory(selected.getValue())
                    ? selected.getValue()
                    : selected.getValue().getParent();
            var dialog = new TextInputDialog("New Folder");
            dialog.setTitle("New Folder");
            dialog.setHeaderText("Enter folder name:");
            dialog.showAndWait().ifPresent(name -> {
                try {
                    Files.createDirectories(dir.resolve(name));
                    loadVault(vault); // refresh tree
                } catch (IOException ignored) {}
            });
        });

        var delete = new MenuItem("Delete");
        delete.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + selected.getValue().getFileName() + "?");
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        vaultManager.deleteNote(selected.getValue());
                    } catch (IOException ignored) {}
                }
            });
        });

        var reveal = new MenuItem("Reveal in File Manager");
        reveal.setOnAction(e -> {
            var selected = treeView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    var desktop = java.awt.Desktop.getDesktop();
                    desktop.open(selected.getValue().getParent().toFile());
                } catch (Exception ignored) {}
            }
        });

        treeView.setContextMenu(new ContextMenu(newNote, newFolder, delete, new SeparatorMenuItem(), reveal));
    }

    private void subscribeToEvents() {
        eventBus.subscribe(NoteCreated.class, e -> Platform.runLater(() -> {
            if (vault != null) loadVault(vault);
        }));
        eventBus.subscribe(NoteDeleted.class, e -> Platform.runLater(() -> {
            if (vault != null) loadVault(vault);
        }));
        eventBus.subscribe(NoteRenamed.class, e -> Platform.runLater(() -> {
            if (vault != null) loadVault(vault);
        }));
    }

    private String fileToTitle(Path path) {
        var name = path.getFileName().toString();
        return name.endsWith(".md") ? name.substring(0, name.length() - 3) : name;
    }

    private static class FileTreeCell extends TreeCell<Path> {
        @Override
        protected void updateItem(Path path, boolean empty) {
            super.updateItem(path, empty);
            if (empty || path == null) {
                setText(null);
                setGraphic(null);
            } else if (Files.isDirectory(path)) {
                setText(path.getFileName().toString());
            } else {
                var name = path.getFileName().toString();
                setText(name.endsWith(".md") ? name.substring(0, name.length() - 3) : name);
            }
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/app/orgx/desktop/ui/FileTreePanel.java
git commit -m "Add FileTreePanel with TreeView, context menu, and event handling"
```

---

### Task 14: BacklinksPanel

**Files:**
- Create: `src/main/java/app/orgx/desktop/ui/BacklinksPanel.java`

- [ ] **Step 1: Implement BacklinksPanel**

```java
package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.LinkClicked;
import app.orgx.desktop.core.events.NoteOpened;
import app.orgx.desktop.core.events.NoteSaved;
import app.orgx.desktop.model.Link;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.vault.LinkIndex;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class BacklinksPanel extends VBox {

    private final EventBus eventBus;
    private final LinkIndex linkIndex;
    private final VBox cardContainer;
    private final Label headerLabel;

    public BacklinksPanel(EventBus eventBus, LinkIndex linkIndex) {
        this.eventBus = eventBus;
        this.linkIndex = linkIndex;
        getStyleClass().add("backlinks-panel");
        setPadding(new Insets(8));
        setSpacing(8);

        headerLabel = new Label("Backlinks");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        cardContainer = new VBox(8);
        var scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().addAll(headerLabel, scrollPane);
        scrollPane.prefHeightProperty().bind(heightProperty().subtract(40));

        eventBus.subscribe(NoteOpened.class, e -> Platform.runLater(() -> refresh(e.note())));
        eventBus.subscribe(NoteSaved.class, e -> Platform.runLater(() -> refresh(e.note())));
    }

    private void refresh(Note currentNote) {
        if (currentNote == null) {
            cardContainer.getChildren().clear();
            headerLabel.setText("Backlinks");
            return;
        }

        var backlinks = linkIndex.getBacklinks(currentNote);
        headerLabel.setText("Backlinks (" + backlinks.size() + ")");
        cardContainer.getChildren().clear();

        if (backlinks.isEmpty()) {
            var empty = new Label("No backlinks");
            empty.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            cardContainer.getChildren().add(empty);
            return;
        }

        for (var link : backlinks) {
            cardContainer.getChildren().add(createCard(link));
        }
    }

    private VBox createCard(Link link) {
        var card = new VBox(4);
        card.getStyleClass().add("backlink-card");

        var sourceName = link.source().getFileName().toString();
        var title = sourceName.endsWith(".md")
                ? sourceName.substring(0, sourceName.length() - 3)
                : sourceName;

        var titleLabel = new Label(title);
        titleLabel.getStyleClass().add("backlink-title");

        var contextLabel = new Label(link.context());
        contextLabel.getStyleClass().add("backlink-context");
        contextLabel.setWrapText(true);
        contextLabel.setMaxHeight(60);

        card.getChildren().addAll(titleLabel, contextLabel);
        card.setOnMouseClicked(e ->
                eventBus.publish(new LinkClicked(title)));

        return card;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/app/orgx/desktop/ui/BacklinksPanel.java
git commit -m "Add BacklinksPanel with backlink cards and event subscriptions"
```

---

### Task 15: Command Palette — Core + Providers

**Files:**
- Create: `src/main/java/app/orgx/desktop/ui/palette/PaletteProvider.java`
- Create: `src/main/java/app/orgx/desktop/ui/palette/PaletteResult.java`
- Create: `src/main/java/app/orgx/desktop/ui/palette/FileSearchProvider.java`
- Create: `src/main/java/app/orgx/desktop/ui/palette/ContentSearchProvider.java`
- Create: `src/main/java/app/orgx/desktop/ui/palette/CommandProvider.java`
- Create: `src/main/java/app/orgx/desktop/ui/palette/VaultSwitchProvider.java`
- Create: `src/main/java/app/orgx/desktop/ui/CommandPalette.java`

- [ ] **Step 1: Create PaletteResult**

```java
package app.orgx.desktop.ui.palette;

import javafx.scene.Node;

public record PaletteResult(Node icon, String title, String subtitle, Runnable action) {}
```

- [ ] **Step 2: Create PaletteProvider interface**

```java
package app.orgx.desktop.ui.palette;

import java.util.List;

public interface PaletteProvider {
    boolean matches(String rawInput);
    List<PaletteResult> search(String query);
    default String stripPrefix(String rawInput) { return rawInput; }
}
```

- [ ] **Step 3: Create FileSearchProvider**

```java
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
```

- [ ] **Step 4: Create ContentSearchProvider**

```java
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
```

- [ ] **Step 5: Create CommandProvider**

```java
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
```

- [ ] **Step 6: Create VaultSwitchProvider**

```java
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
```

- [ ] **Step 7: Create CommandPalette**

```java
package app.orgx.desktop.ui;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.CommandPaletteClosed;
import app.orgx.desktop.core.events.CommandPaletteOpened;
import app.orgx.desktop.ui.palette.PaletteProvider;
import app.orgx.desktop.ui.palette.PaletteResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.util.List;

public class CommandPalette extends VBox {

    private final TextField searchField;
    private final ListView<PaletteResult> resultsList;
    private final List<PaletteProvider> providers;
    private final EventBus eventBus;

    public CommandPalette(EventBus eventBus, List<PaletteProvider> providers) {
        this.eventBus = eventBus;
        this.providers = providers;

        getStyleClass().add("command-palette");
        setMaxWidth(600);
        setMaxHeight(400);
        setAlignment(Pos.TOP_CENTER);

        searchField = new TextField();
        searchField.setPromptText("Search notes, commands, vaults...");

        resultsList = new ListView<>();
        resultsList.setCellFactory(lv -> new PaletteResultCell());
        resultsList.setMaxHeight(350);

        getChildren().addAll(searchField, resultsList);

        searchField.textProperty().addListener((obs, old, text) -> updateResults(text));

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hide();
            } else if (e.getCode() == KeyCode.ENTER) {
                executeSelected();
            } else if (e.getCode() == KeyCode.DOWN) {
                resultsList.requestFocus();
                if (!resultsList.getItems().isEmpty()) {
                    resultsList.getSelectionModel().selectFirst();
                }
                e.consume();
            }
        });

        resultsList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hide();
            } else if (e.getCode() == KeyCode.ENTER) {
                executeSelected();
            }
        });

        resultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                executeSelected();
            }
        });

        setVisible(false);
        setManaged(false);
    }

    public void show() {
        searchField.clear();
        resultsList.getItems().clear();
        setVisible(true);
        setManaged(true);
        searchField.requestFocus();
        eventBus.publish(new CommandPaletteOpened());
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        eventBus.publish(new CommandPaletteClosed());
    }

    public boolean isShowing() {
        return isVisible();
    }

    private void updateResults(String rawInput) {
        resultsList.getItems().clear();
        if (rawInput == null || rawInput.isBlank()) return;

        for (var provider : providers) {
            if (provider.matches(rawInput)) {
                var query = provider.stripPrefix(rawInput);
                resultsList.getItems().addAll(provider.search(query));
                break;
            }
        }

        if (!resultsList.getItems().isEmpty()) {
            resultsList.getSelectionModel().selectFirst();
        }
    }

    private void executeSelected() {
        var selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            hide();
            selected.action().run();
        }
    }

    private static class PaletteResultCell extends ListCell<PaletteResult> {
        @Override
        protected void updateItem(PaletteResult result, boolean empty) {
            super.updateItem(result, empty);
            if (empty || result == null) {
                setGraphic(null);
                return;
            }

            var titleLabel = new Label(result.title());
            titleLabel.getStyleClass().add("palette-result-title");

            var subtitleLabel = new Label(result.subtitle());
            subtitleLabel.getStyleClass().add("palette-result-subtitle");

            var textBox = new VBox(2, titleLabel, subtitleLabel);
            var row = new HBox(8, result.icon(), textBox);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 8, 4, 8));

            setGraphic(row);
        }
    }
}
```

- [ ] **Step 8: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add src/main/java/app/orgx/desktop/ui/palette/ src/main/java/app/orgx/desktop/ui/CommandPalette.java
git commit -m "Add CommandPalette with file search, content search, commands, and vault switch providers"
```

---

### Task 16: MarkdownToModel — commonmark AST to RichTextModel

**Files:**
- Create: `src/main/java/app/orgx/desktop/markdown/MarkdownToModel.java`
- Create: `src/test/java/app/orgx/desktop/markdown/MarkdownToModelTest.java`

This is the most complex task. It converts parsed markdown into a `RichTextModel` for the RichTextArea. Consult the RichTextArea demo source at `/home/moamen/git-repos/jfx/apps/samples/RichTextAreaDemo/src/com/oracle/demo/richtext/` for API usage, especially:
- `rta/DemoModel.java` — how to build models with styles
- `rta/ParagraphAttributesDemoModel.java` — paragraph-level styling
- `editor/RichEditorDemoWindow.java` — how models are used with RichTextArea

- [ ] **Step 1: Write MarkdownToModel tests**

Tests verify the conversion from markdown string to a model that can be set on a RichTextArea. Since `RichTextModel` is a JavaFX class and may require the toolkit, tests focus on verifying the parser integration and the structure of the output. If JavaFX toolkit initialization is needed, use `Platform.startup()` in a `@BeforeAll`.

```java
package app.orgx.desktop.markdown;

import org.commonmark.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownToModelTest {

    private MarkdownToModel converter;

    @BeforeEach
    void setUp() {
        converter = new MarkdownToModel();
    }

    @Test
    void convertsPlainText() {
        var result = converter.convert("Hello world");
        assertNotNull(result);
        // Model should exist and have content
        assertNotNull(result.model());
    }

    @Test
    void extractsWikiLinks() {
        var result = converter.convert("See [[My Note]] and [[Other]]");
        // Verify wikilinks were found during conversion
        assertEquals(2, result.wikiLinks().size());
        assertTrue(result.wikiLinks().contains("My Note"));
        assertTrue(result.wikiLinks().contains("Other"));
    }

    @Test
    void handlesHeadings() {
        var result = converter.convert("# Heading 1\n\nSome text\n\n## Heading 2");
        assertNotNull(result.model());
        // Verify heading levels were tracked
        assertTrue(result.headingCount() >= 2);
    }

    @Test
    void handlesEmptyInput() {
        var result = converter.convert("");
        assertNotNull(result.model());
    }

    @Test
    void handlesCodeBlock() {
        var result = converter.convert("```java\nint x = 1;\n```");
        assertNotNull(result.model());
    }
}
```

Note: The exact assertions depend on how `MarkdownToModel.Result` is structured. The tests above verify the conversion doesn't crash and tracks metadata. More detailed tests (verifying specific style attributes on segments) should be added once the RichTextModel API is confirmed to work as expected. These tests may need to run with the JavaFX toolkit initialized.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.markdown.MarkdownToModelTest"`
Expected: Compilation error — MarkdownToModel does not exist

- [ ] **Step 3: Implement MarkdownToModel**

This is a large class. It walks the commonmark AST and builds a `RichTextModel` using the segment-based API. Consult the RichTextArea demo source for exact API calls.

```java
package app.orgx.desktop.markdown;

import jfx.incubator.scene.control.richtext.model.RichTextModel;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;

import java.util.*;

public class MarkdownToModel {

    private final Parser parser;

    public MarkdownToModel() {
        List<Extension> extensions = List.of(WikiLinkExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
    }

    public Result convert(String markdown) {
        var document = parser.parse(markdown);
        var builder = new ModelBuilder();
        builder.visit(document);
        return new Result(builder.build(), builder.wikiLinks, builder.headingCount);
    }

    public record Result(RichTextModel model, Set<String> wikiLinks, int headingCount) {}

    private static class ModelBuilder extends AbstractVisitor {
        private final RichTextModel model = new RichTextModel();
        private final Set<String> wikiLinks = new LinkedHashSet<>();
        private int headingCount = 0;
        private final Deque<StyleAttributeMap> styleStack = new ArrayDeque<>();
        private boolean firstParagraph = true;

        ModelBuilder() {
            styleStack.push(StyleAttributeMap.EMPTY);
        }

        RichTextModel build() {
            return model;
        }

        @Override
        public void visit(Heading heading) {
            headingCount++;
            var level = heading.getLevel();
            var fontSize = switch (level) {
                case 1 -> 28.0;
                case 2 -> 24.0;
                case 3 -> 20.0;
                case 4 -> 18.0;
                case 5 -> 16.0;
                default -> 14.0;
            };

            // Push heading style
            var style = StyleAttributeMap.builder()
                    .setBold(true)
                    .setFontSize(fontSize)
                    .build();
            styleStack.push(style);

            // Add the markdown syntax (hidden in WYSIWYG)
            var prefix = "#".repeat(level) + " ";
            appendText(prefix, StyleAttributeMap.builder()
                    .setBold(true)
                    .setFontSize(fontSize)
                    .build());

            visitChildren(heading);
            styleStack.pop();
            appendNewline();
        }

        @Override
        public void visit(Paragraph node) {
            if (!firstParagraph) {
                appendNewline();
            }
            firstParagraph = false;
            visitChildren(node);
            appendNewline();
        }

        @Override
        public void visit(Text text) {
            appendText(text.getLiteral(), currentStyle());
        }

        @Override
        public void visit(Emphasis emphasis) {
            var style = StyleAttributeMap.builder().setItalic(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            appendText("*", currentStyle());
            visitChildren(emphasis);
            appendText("*", currentStyle());
            styleStack.pop();
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            var style = StyleAttributeMap.builder().setBold(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            appendText("**", currentStyle());
            visitChildren(strongEmphasis);
            appendText("**", currentStyle());
            styleStack.pop();
        }

        @Override
        public void visit(Code code) {
            var style = StyleAttributeMap.builder()
                    .setFontFamily("Monospace")
                    .build();
            appendText("`", style);
            appendText(code.getLiteral(), style);
            appendText("`", style);
        }

        @Override
        public void visit(FencedCodeBlock codeBlock) {
            var style = StyleAttributeMap.builder()
                    .setFontFamily("Monospace")
                    .build();
            appendText("```" + (codeBlock.getInfo() != null ? codeBlock.getInfo() : ""), style);
            appendNewline();
            appendText(codeBlock.getLiteral(), style);
            appendText("```", style);
            appendNewline();
        }

        @Override
        public void visit(BulletList bulletList) {
            visitChildren(bulletList);
        }

        @Override
        public void visit(OrderedList orderedList) {
            visitChildren(orderedList);
        }

        @Override
        public void visit(ListItem listItem) {
            appendText("- ", currentStyle());
            visitChildren(listItem);
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            appendText("> ", currentStyle());
            var style = StyleAttributeMap.builder().setItalic(true).build();
            styleStack.push(mergeStyles(currentStyle(), style));
            visitChildren(blockQuote);
            styleStack.pop();
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            appendText("---", currentStyle());
            appendNewline();
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            appendNewline();
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            appendNewline();
        }

        @Override
        public void visit(CustomNode node) {
            if (node instanceof WikiLinkNode wikiLink) {
                wikiLinks.add(wikiLink.getTarget());
                // Style wikilinks distinctly
                var style = StyleAttributeMap.builder().build();
                // Note: custom WIKILINK_TARGET attribute would go here
                // For now, use a visually distinct style
                appendText("[[", style);
                appendText(wikiLink.getTarget(), style);
                appendText("]]", style);
            } else {
                visitChildren(node);
            }
        }

        @Override
        public void visit(Link link) {
            appendText("[", currentStyle());
            visitChildren(link);
            appendText("](" + link.getDestination() + ")", currentStyle());
        }

        @Override
        public void visit(Image image) {
            var alt = image.getTitle() != null ? image.getTitle() : "";
            appendText("![" + alt + "](" + image.getDestination() + ")", currentStyle());
        }

        private StyleAttributeMap currentStyle() {
            return styleStack.peek();
        }

        private void appendText(String text, StyleAttributeMap style) {
            model.appendText(text, style);
        }

        private void appendNewline() {
            model.appendText("\n", StyleAttributeMap.EMPTY);
        }

        private StyleAttributeMap mergeStyles(StyleAttributeMap base, StyleAttributeMap overlay) {
            // Merge overlay attributes into base
            // This is a simplification — the real implementation should merge all attributes
            return overlay;
        }
    }
}
```

**Important:** The `RichTextModel` API (methods like `appendText`, `StyleAttributeMap.builder()`, etc.) must be verified against the actual JavaFX 26 source. Consult:
- `/home/moamen/git-repos/jfx/apps/samples/RichTextAreaDemo/src/com/oracle/demo/richtext/rta/DemoModel.java`
- The `SimpleViewOnlyStyledModel` builder API
- The `RichTextModel` class itself

The implementation above is a skeleton. The exact method names and builder patterns must match the real API. Adjust as needed when compilation is attempted.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.markdown.MarkdownToModelTest"`
Expected: All 5 tests PASS (may need JavaFX toolkit init)

If tests fail due to JavaFX toolkit not being initialized, add to the test class:

```java
@BeforeAll
static void initToolkit() {
    try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/markdown/MarkdownToModel.java src/test/java/app/orgx/desktop/markdown/MarkdownToModelTest.java
git commit -m "Add MarkdownToModel converter from commonmark AST to RichTextModel"
```

---

### Task 17: ModelToMarkdown — RichTextModel to Markdown String

**Files:**
- Create: `src/main/java/app/orgx/desktop/markdown/ModelToMarkdown.java`
- Create: `src/test/java/app/orgx/desktop/markdown/ModelToMarkdownTest.java`

- [ ] **Step 1: Write round-trip tests**

The best test for the serializer is round-trip: markdown → model → markdown should produce equivalent output.

```java
package app.orgx.desktop.markdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelToMarkdownTest {

    private MarkdownToModel toModel;
    private ModelToMarkdown toMarkdown;

    @BeforeEach
    void setUp() {
        toModel = new MarkdownToModel();
        toMarkdown = new ModelToMarkdown();
    }

    @Test
    void roundTripsPlainText() {
        assertRoundTrip("Hello world");
    }

    @Test
    void roundTripsHeading() {
        assertRoundTrip("# Heading 1");
    }

    @Test
    void roundTripsBold() {
        assertRoundTrip("Some **bold** text");
    }

    @Test
    void roundTripsItalic() {
        assertRoundTrip("Some *italic* text");
    }

    @Test
    void roundTripsWikiLink() {
        assertRoundTrip("See [[My Note]] here");
    }

    @Test
    void roundTripsCodeBlock() {
        assertRoundTrip("```java\nint x = 1;\n```");
    }

    @Test
    void roundTripsBulletList() {
        assertRoundTrip("- Item one\n- Item two");
    }

    private void assertRoundTrip(String markdown) {
        var result = toModel.convert(markdown);
        var output = toMarkdown.convert(result.model());
        assertEquals(markdown.strip(), output.strip());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest="app.orgx.desktop.markdown.ModelToMarkdownTest"`
Expected: Compilation error — ModelToMarkdown does not exist

- [ ] **Step 3: Implement ModelToMarkdown**

Since the `MarkdownToModel` preserves the full markdown source in the model (syntax markers are styled but present), serialization walks the model text and outputs it directly. The key insight from the spec: "The source markdown is preserved as the canonical representation."

```java
package app.orgx.desktop.markdown;

import jfx.incubator.scene.control.richtext.model.RichTextModel;

public class ModelToMarkdown {

    public String convert(RichTextModel model) {
        // Since the model contains the full markdown source (including syntax markers),
        // we extract the plain text content which IS the markdown.
        var sb = new StringBuilder();
        var paragraphCount = model.getParagraphCount();
        for (int i = 0; i < paragraphCount; i++) {
            if (i > 0) sb.append("\n");
            sb.append(model.getPlainText(i));
        }
        return sb.toString();
    }
}
```

**Important:** The exact `RichTextModel` API for iterating paragraphs and extracting plain text must be verified. The demo source at `/home/moamen/git-repos/jfx/apps/samples/RichTextAreaDemo/src/com/oracle/demo/richtext/` shows methods like `getPlainText(int paragraphIndex)` and `getParagraphCount()`. Verify and adjust.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest="app.orgx.desktop.markdown.ModelToMarkdownTest"`
Expected: All 7 tests PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/markdown/ModelToMarkdown.java src/test/java/app/orgx/desktop/markdown/ModelToMarkdownTest.java
git commit -m "Add ModelToMarkdown serializer with round-trip fidelity"
```

---

### Task 18: NoteEditor Service & NoteEditorPanel

**Files:**
- Create: `src/main/java/app/orgx/desktop/editor/NoteEditor.java`
- Create: `src/main/java/app/orgx/desktop/editor/NoteEditorPanel.java`

- [ ] **Step 1: Create NoteEditor service**

```java
package app.orgx.desktop.editor;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.NoteSaved;
import app.orgx.desktop.markdown.MarkdownToModel;
import app.orgx.desktop.markdown.ModelToMarkdown;
import app.orgx.desktop.model.Note;
import app.orgx.desktop.vault.VaultManager;
import jfx.incubator.scene.control.richtext.model.RichTextModel;

import java.io.IOException;
import java.nio.file.Files;

public class NoteEditor {

    private final EventBus eventBus;
    private final VaultManager vaultManager;
    private final MarkdownToModel markdownToModel;
    private final ModelToMarkdown modelToMarkdown;

    private Note currentNote;
    private RichTextModel currentModel;
    private boolean modified;
    private String originalContent;

    public NoteEditor(EventBus eventBus, VaultManager vaultManager) {
        this.eventBus = eventBus;
        this.vaultManager = vaultManager;
        this.markdownToModel = new MarkdownToModel();
        this.modelToMarkdown = new ModelToMarkdown();
    }

    public RichTextModel open(Note note) {
        this.currentNote = note;
        this.originalContent = note.content();
        this.modified = false;

        var result = markdownToModel.convert(note.content());
        this.currentModel = result.model();
        return currentModel;
    }

    public void save() throws IOException {
        if (currentNote == null || currentModel == null) return;

        var markdown = modelToMarkdown.convert(currentModel);
        Files.writeString(currentNote.path(), markdown);
        currentNote.setContent(markdown);
        originalContent = markdown;
        modified = false;

        // Re-extract links and update vault
        var links = VaultManager.extractLinks(markdown);
        currentNote.setLinks(links);

        eventBus.publish(new NoteSaved(currentNote));
    }

    public void markModified() {
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public Note getCurrentNote() {
        return currentNote;
    }

    public RichTextModel getCurrentModel() {
        return currentModel;
    }

    public void close() {
        currentNote = null;
        currentModel = null;
        modified = false;
        originalContent = null;
    }
}
```

- [ ] **Step 2: Create NoteEditorPanel**

```java
package app.orgx.desktop.editor;

import app.orgx.desktop.core.EventBus;
import app.orgx.desktop.core.events.LinkClicked;
import app.orgx.desktop.core.events.NoteOpened;
import app.orgx.desktop.model.Note;
import javafx.application.Platform;
import jfx.incubator.scene.control.richtext.RichTextArea;
import javafx.scene.layout.StackPane;

public class NoteEditorPanel extends StackPane {

    private final RichTextArea richTextArea;
    private final NoteEditor noteEditor;
    private final EventBus eventBus;

    public NoteEditorPanel(EventBus eventBus, NoteEditor noteEditor) {
        this.eventBus = eventBus;
        this.noteEditor = noteEditor;
        getStyleClass().add("note-editor");

        richTextArea = new RichTextArea();
        richTextArea.setWrapText(true);

        getChildren().add(richTextArea);

        subscribeToEvents();
    }

    private void subscribeToEvents() {
        eventBus.subscribe(NoteOpened.class, e -> Platform.runLater(() -> openNote(e.note())));
    }

    private void openNote(Note note) {
        if (note == null) return;
        var model = noteEditor.open(note);
        richTextArea.setModel(model);
    }

    public RichTextArea getRichTextArea() {
        return richTextArea;
    }
}
```

**Note:** The `RichTextArea` is in the incubator module: `jfx.incubator.scene.control.richtext.RichTextArea`. The import used above is correct. If the `jfx.incubator.richtext` module is not found at compile time, ensure the pom.xml includes the correct incubator artifact and `module-info.java` has `requires jfx.incubator.richtext;`.

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/app/orgx/desktop/editor/
git commit -m "Add NoteEditor service and NoteEditorPanel with RichTextArea"
```

---

### Task 19: AppContext — Wire Everything Together

**Files:**
- Create: `src/main/java/app/orgx/desktop/core/AppContext.java`
- Modify: `src/main/java/app/orgx/desktop/OrgxApplication.java`

- [ ] **Step 1: Create AppContext**

```java
package app.orgx.desktop.core;

import app.orgx.desktop.config.ConfigManager;
import app.orgx.desktop.editor.NoteEditor;
import app.orgx.desktop.editor.NoteEditorPanel;
import app.orgx.desktop.model.VaultConfig;
import app.orgx.desktop.search.SearchEngine;
import app.orgx.desktop.ui.*;
import app.orgx.desktop.ui.palette.*;
import app.orgx.desktop.vault.*;
import app.orgx.desktop.core.events.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AppContext {

    private final EventBus eventBus;
    private final ConfigManager configManager;
    private final VaultManager vaultManager;
    private final LinkIndex linkIndex;
    private final SearchEngine searchEngine;
    private final NavigationHistory navigationHistory;
    private final NoteEditor noteEditor;
    private FileWatcher fileWatcher;

    // UI components
    private AppShell appShell;
    private FileTreePanel fileTreePanel;
    private NoteEditorPanel noteEditorPanel;
    private BacklinksPanel backlinksPanel;
    private CommandPalette commandPalette;

    public AppContext() throws IOException {
        this.eventBus = new EventBus();
        this.configManager = ConfigManager.createDefault();
        this.vaultManager = new VaultManager(eventBus);
        this.linkIndex = new LinkIndex();
        this.searchEngine = new SearchEngine();
        this.navigationHistory = new NavigationHistory();
        this.noteEditor = new NoteEditor(eventBus, vaultManager);

        wireEvents();
    }

    public void initUI() {
        appShell = new AppShell(eventBus);
        fileTreePanel = new FileTreePanel(eventBus, vaultManager);
        noteEditorPanel = new NoteEditorPanel(eventBus, noteEditor);
        backlinksPanel = new BacklinksPanel(eventBus, linkIndex);

        appShell.setFileTreePanel(fileTreePanel);
        appShell.setEditorPanel(noteEditorPanel);
        appShell.setBacklinksPanel(backlinksPanel);

        // Setup command palette
        var commandProvider = new CommandProvider();
        registerCommands(commandProvider);

        commandPalette = new CommandPalette(eventBus, List.of(
                new FileSearchProvider(searchEngine, this::openNoteByTitle),
                new ContentSearchProvider(searchEngine, this::openNoteByTitle),
                commandProvider,
                new VaultSwitchProvider(
                        () -> configManager.load().vaults(),
                        this::switchVault)
        ));
        appShell.getCenterStack().getChildren().add(commandPalette);

        // Apply config
        var config = configManager.load();
        appShell.setStatusBarVisible(config.showStatusBar());
    }

    public AppShell getAppShell() { return appShell; }
    public EventBus getEventBus() { return eventBus; }
    public CommandPalette getCommandPalette() { return commandPalette; }
    public ConfigManager getConfigManager() { return configManager; }
    public NoteEditor getNoteEditor() { return noteEditor; }
    public NavigationHistory getNavigationHistory() { return navigationHistory; }
    public void setNavigatingHistory(boolean v) { this.navigatingHistory = v; }

    public void openVault(Path path) throws IOException {
        if (fileWatcher != null) fileWatcher.stop();
        vaultManager.openVault(path);
        linkIndex.rebuild(vaultManager.getVault());
        searchEngine.rebuildIndex(vaultManager.getVault());
        fileTreePanel.loadVault(vaultManager.getVault());

        fileWatcher = new FileWatcher(path, eventBus);
        fileWatcher.start();
    }

    // Flag to prevent navigation history infinite loop:
    // NoteOpened → push to history, but back/forward also publish NoteOpened
    private boolean navigatingHistory = false;

    private void wireEvents() {
        // When a note is opened (not via back/forward), push to navigation history
        eventBus.subscribe(NoteOpened.class, e -> {
            if (!navigatingHistory) {
                navigationHistory.push(e.note());
            }
        });

        // When a link is clicked, resolve and open
        eventBus.subscribe(LinkClicked.class, e -> {
            var vault = vaultManager.getVault();
            if (vault == null) return;
            var note = vault.getNote(e.target());
            if (note != null) {
                eventBus.publish(new NoteOpened(note));
            } else {
                // Create new note for non-existent link
                try {
                    var newNote = vaultManager.createNote(e.target());
                    eventBus.publish(new NoteOpened(newNote));
                } catch (IOException ignored) {}
            }
        });

        // When a note is saved, update link index and search
        eventBus.subscribe(NoteSaved.class, e -> {
            linkIndex.updateNote(e.note());
            try {
                searchEngine.updateNote(e.note());
            } catch (IOException ignored) {}
        });

        // External file changes
        eventBus.subscribe(NoteExternallyChanged.class, e -> {
            try {
                vaultManager.loadNote(e.path());
            } catch (IOException ignored) {}
        });
    }

    private void registerCommands(CommandProvider provider) {
        provider.register("New Note", "Create a new note", () -> {
            // Trigger new note flow
        });
        provider.register("Toggle File Tree", "Show/hide file tree sidebar", () -> appShell.toggleFileTree());
        provider.register("Toggle Backlinks", "Show/hide backlinks panel", () -> appShell.toggleBacklinks());
        provider.register("Toggle Status Bar", "Show/hide status bar", () -> {
            var config = configManager.load();
            config.setShowStatusBar(!config.showStatusBar());
            configManager.save(config);
            appShell.setStatusBarVisible(config.showStatusBar());
        });
        provider.register("Switch Theme", "Toggle light/dark theme", () -> {
            var config = configManager.load();
            var newTheme = "light".equals(config.theme()) ? "dark" : "light";
            config.setTheme(newTheme);
            configManager.save(config);
            eventBus.publish(new ThemeChanged(newTheme));
        });
        provider.register("Delete Note", "Delete the current note", () -> {
            var note = noteEditor.getCurrentNote();
            if (note != null) {
                try {
                    vaultManager.deleteNote(note.path());
                    noteEditor.close();
                } catch (IOException ignored) {}
            }
        });
        provider.register("Rename Note", "Rename the current note", () -> {
            // TODO: show rename dialog, call vaultManager.renameNote()
        });
        provider.register("Open Vault Folder", "Open vault in file manager", () -> {
            var vault = vaultManager.getVault();
            if (vault != null) {
                try {
                    java.awt.Desktop.getDesktop().open(vault.rootPath().toFile());
                } catch (Exception ignored) {}
            }
        });
        provider.register("Reload Vault", "Re-scan vault files", () -> {
            var vault = vaultManager.getVault();
            if (vault != null) {
                try {
                    openVault(vault.rootPath());
                } catch (IOException ignored) {}
            }
        });
    }

    /** Check dirty state before switching notes. Returns true if safe to proceed. */
    public boolean checkDirtyState(javafx.stage.Window owner) {
        if (!noteEditor.isModified()) return true;
        var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Save changes to " + noteEditor.getCurrentNote().title() + "?");
        alert.getButtonTypes().setAll(
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO,
                javafx.scene.control.ButtonType.CANCEL);
        var result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == javafx.scene.control.ButtonType.YES) {
                try { noteEditor.save(); } catch (IOException ignored) {}
                return true;
            } else if (result.get() == javafx.scene.control.ButtonType.NO) {
                return true; // discard
            }
        }
        return false; // cancel
    }

    private void openNoteByTitle(String title) {
        var vault = vaultManager.getVault();
        if (vault == null) return;
        var note = vault.getNote(title);
        if (note != null) {
            eventBus.publish(new NoteOpened(note));
        }
    }

    private void switchVault(app.orgx.desktop.model.VaultEntry entry) {
        try {
            openVault(entry.path());
            var config = configManager.load();
            config.setLastOpenedVault(entry.name());
            configManager.save(config);
        } catch (IOException ignored) {}
    }

    public void shutdown() throws IOException {
        if (fileWatcher != null) fileWatcher.stop();
        searchEngine.close();
    }
}
```

- [ ] **Step 2: Update OrgxApplication to use AppContext**

```java
package app.orgx.desktop;

import app.orgx.desktop.core.AppContext;
import app.orgx.desktop.core.events.ThemeChanged;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class OrgxApplication extends Application {

    private static final Logger log = LogManager.getLogger(OrgxApplication.class);
    private AppContext context;

    @Override
    public void start(Stage stage) {
        log.info("Starting application");

        try {
            context = new AppContext();
            context.initUI();
        } catch (IOException e) {
            log.error("Failed to initialize application", e);
            return;
        }

        var shell = context.getAppShell();
        var config = context.getConfigManager().load();

        var scene = new Scene(shell, 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/themes/" + config.theme() + ".css").toExternalForm());

        // Theme switching
        context.getEventBus().subscribe(ThemeChanged.class, e -> {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/themes/" + e.theme() + ".css").toExternalForm());
        });

        // Global keyboard shortcuts
        setupKeyboardShortcuts(scene);

        stage.setScene(scene);
        stage.setTitle("Orgx");
        stage.setOnCloseRequest(e -> {
            try {
                context.shutdown();
            } catch (IOException ignored) {}
        });
        stage.show();

        // Open last vault if configured
        if (!config.lastOpenedVault().isBlank()) {
            var vaultEntry = config.vaults().stream()
                    .filter(v -> v.name().equals(config.lastOpenedVault()))
                    .findFirst();
            vaultEntry.ifPresent(v -> {
                try {
                    context.openVault(v.path());
                } catch (IOException ignored) {}
            });
        }
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // Ctrl+P — Command palette
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                var palette = context.getCommandPalette();
                if (palette.isShowing()) palette.hide();
                else palette.show();
                e.consume();
            }
            // Ctrl+B — Toggle file tree
            else if (e.isControlDown() && !e.isShiftDown() && e.getCode() == KeyCode.B) {
                context.getAppShell().toggleFileTree();
                e.consume();
            }
            // Ctrl+Shift+B — Toggle backlinks
            else if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.B) {
                context.getAppShell().toggleBacklinks();
                e.consume();
            }
            // Ctrl+S — Save
            else if (e.isControlDown() && e.getCode() == KeyCode.S) {
                try {
                    context.getNoteEditor().save();
                } catch (IOException ignored) {}
                e.consume();
            }
            // Ctrl+W — Close current note
            else if (e.isControlDown() && e.getCode() == KeyCode.W) {
                if (context.checkDirtyState(stage)) {
                    context.getNoteEditor().close();
                }
                e.consume();
            }
            // Ctrl+N — New note
            else if (e.isControlDown() && e.getCode() == KeyCode.N) {
                // TODO: new note dialog
                e.consume();
            }
            // Alt+Left — Navigate back
            else if (e.isAltDown() && e.getCode() == KeyCode.LEFT) {
                var history = context.getNavigationHistory();
                if (history.canGoBack()) {
                    context.setNavigatingHistory(true);
                    var note = history.back();
                    context.getEventBus().publish(
                            new app.orgx.desktop.core.events.NoteOpened(note));
                    context.setNavigatingHistory(false);
                }
                e.consume();
            }
            // Alt+Right — Navigate forward
            else if (e.isAltDown() && e.getCode() == KeyCode.RIGHT) {
                var history = context.getNavigationHistory();
                if (history.canGoForward()) {
                    context.setNavigatingHistory(true);
                    var note = history.forward();
                    context.getEventBus().publish(
                            new app.orgx.desktop.core.events.NoteOpened(note));
                    context.setNavigatingHistory(false);
                }
                e.consume();
            }
            // Ctrl+Shift+F — Content search
            else if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.F) {
                var palette = context.getCommandPalette();
                palette.show();
                // TODO: pre-fill with "?" prefix
                e.consume();
            }
        });
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Smoke test**

Run: `./mvnw javafx:run`
Expected: App opens with the three-panel layout. No vault loaded yet, so panels are empty. `Ctrl+P` should toggle the command palette overlay.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/app/orgx/desktop/core/AppContext.java src/main/java/app/orgx/desktop/OrgxApplication.java
git commit -m "Wire AppContext with all services, UI components, and keyboard shortcuts"
```

---

### Task 20: End-to-End Integration Smoke Test

This task verifies the full flow works: open vault → file tree loads → click note → editor renders → backlinks update → save.

- [ ] **Step 1: Create a test vault**

```bash
mkdir -p /tmp/orgx-test-vault/projects
echo "# Welcome\n\nThis is the start. See [[Project Alpha]]." > /tmp/orgx-test-vault/welcome.md
echo "# Project Alpha\n\nDetails about the project.\nAlso see [[Welcome]]." > /tmp/orgx-test-vault/projects/Project\ Alpha.md
echo "# Random Note\n\nNo links here." > /tmp/orgx-test-vault/random.md
```

- [ ] **Step 2: Configure the app to open the test vault**

Temporarily hardcode the vault path in `OrgxApplication.start()` for testing, or add the vault via the config. The simplest approach: if no vault is configured, prompt with a `DirectoryChooser`.

Add to `OrgxApplication.start()` after `stage.show()`, replacing the last-vault logic temporarily:

```java
if (config.vaults().isEmpty()) {
    var chooser = new javafx.stage.DirectoryChooser();
    chooser.setTitle("Open Vault");
    var dir = chooser.showDialog(stage);
    if (dir != null) {
        context.openVault(dir.toPath());
    }
}
```

- [ ] **Step 3: Run and test manually**

Run: `./mvnw javafx:run`

Verify:
1. Directory chooser appears → select `/tmp/orgx-test-vault`
2. File tree shows: projects/ folder, welcome, random
3. Click "welcome" → editor loads with "# Welcome" content
4. Backlinks panel shows "Project Alpha" (which links to Welcome)
5. Click "Project Alpha" in backlinks → navigates to that note
6. Backlinks panel updates to show "Welcome" (which links to Project Alpha)
7. `Ctrl+P` opens command palette → type "random" → shows "Random Note" → Enter opens it
8. `Alt+Left` navigates back to "Project Alpha"
9. `Ctrl+B` toggles file tree
10. `Ctrl+S` saves (no crash)

- [ ] **Step 4: Fix any issues found during smoke test**

Address bugs found during the manual test. This is expected — first integration always surfaces issues.

- [ ] **Step 5: Commit fixes**

```bash
git add -u
git commit -m "Fix integration issues found during end-to-end smoke test"
```

- [ ] **Step 6: Clean up — keep the DirectoryChooser fallback**

The directory chooser fallback is actually good UX for first launch. Keep it and also implement vault persistence:

After opening a vault, save it to config:

```java
var config = context.getConfigManager().load();
var vaultEntry = new VaultEntry(dir.getName(), dir.toPath());
if (config.vaults().stream().noneMatch(v -> v.path().equals(dir.toPath()))) {
    config.vaults().add(vaultEntry);
}
config.setLastOpenedVault(dir.getName());
context.getConfigManager().save(config);
```

- [ ] **Step 7: Final commit**

```bash
git add -u
git commit -m "Add vault directory chooser and persist vault selection to config"
```

---

### Task 21: Run All Tests & Final Verification

- [ ] **Step 1: Run full test suite**

Run: `./mvnw test`
Expected: All tests pass

- [ ] **Step 2: Fix any failing tests**

Address failures and commit fixes.

- [ ] **Step 3: Run the app and verify all features**

Run: `./mvnw javafx:run`

Complete checklist:
- [ ] Open vault via directory chooser
- [ ] File tree shows vault contents
- [ ] Click note → renders in editor
- [ ] Wikilinks are visually styled
- [ ] Click wikilink → navigates to linked note
- [ ] Click non-existent wikilink → creates note
- [ ] Backlinks panel updates on navigation
- [ ] `Ctrl+P` → file search works
- [ ] `Ctrl+P` → `?query` content search works
- [ ] `Ctrl+P` → `>` command list works
- [ ] `Ctrl+B` toggles file tree
- [ ] `Ctrl+Shift+B` toggles backlinks
- [ ] `Ctrl+S` saves without error
- [ ] `Alt+Left` / `Alt+Right` navigation history
- [ ] Theme switching via command palette
- [ ] Status bar toggle via command palette
- [ ] App remembers last vault on restart

- [ ] **Step 4: Final commit**

```bash
git add -u
git commit -m "Complete MVP: note-taking app with WYSIWYG editor, wikilinks, backlinks, search, and command palette"
```
