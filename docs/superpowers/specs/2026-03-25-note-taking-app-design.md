# Orgx — Note-Taking App Design Spec

## Overview

A document-centric, Obsidian-like note-taking app built with JavaFX 26 and the new RichTextArea control. Notes are stored as plain markdown files on disk. The app provides WYSIWYG markdown editing, `[[wikilinks]]` with backlinks, full-text search, multi-vault support, and a command palette as the primary interaction hub.

## Paradigm & Storage

- **Document-centric:** Each note is a standalone `.md` file in a vault folder.
- **Markdown on disk:** Plain `.md` files, editable outside the app. No hidden databases or metadata folders.
- **Link resolution:** `[[My Note]]` resolves to `My Note.md` anywhere in the vault by case-insensitive filename match. Ambiguous matches show a palette-style picker to disambiguate.
- **Multi-vault:** Users can open and switch between multiple vaults, IntelliJ-style (vault selector in command palette). Switching vaults prompts to save dirty notes, then closes the current vault entirely before opening the new one.
- **Single-document editor:** One note open at a time. No tabs. Navigation history (`Alt+Left/Right`) provides quick switching between recent notes.

## Layout

```
BorderPane (root)
├── center: StackPane (allows command palette overlay)
│           └── SplitPane (horizontal)
│               ├── FileTreePanel
│               ├── NoteEditorPanel
│               └── BacklinksPanel
└── bottom: StatusBar (toggleable via setting, hidden by default)
```

No header bar. Minimal look. The command palette is the universal entry point for search, navigation, vault switching, and app commands.

```
┌───────────┬─────────────────────────────┬───────────────────┐
│           │                             │                   │
│  File     │       Note Editor           │   Backlinks       │
│  Tree     │                             │                   │
│           │   # My Note                 │   ┌─────────────┐ │
│  > daily  │                             │   │ Daily Log   │ │
│  > proj   │   Some text with a          │   │ "...links   │ │
│    idea   │   [[wikilink]] in it.       │   │ to [[My..." │ │
│    todo   │                             │   └─────────────┘ │
│           │                             │                   │
└───────────┴─────────────────────────────┴───────────────────┘
```

Both sidebars are toggleable via keyboard shortcuts or command palette.

## Command Palette

Opened with `Ctrl+P`. A floating overlay centered near the top of the window.

**Modes by prefix:**

| Input | Mode | Results |
|---|---|---|
| `meeting` | File search (default) | Fuzzy-matched note titles, recent notes boosted |
| `?meeting` | Content search | Notes containing "meeting" with context snippets |
| `>toggle` | Commands | Matching app commands |
| `vault:` | Vault switch | List of known vaults, filterable |

**App commands:** New Note, Delete Note, Rename Note, Toggle File Tree, Toggle Backlinks, Toggle Status Bar, Switch Theme, Switch Vault, Open Vault Folder, Reload Vault.

**Implementation:** `TextField` + `ListView` in a `StackPane` overlay. Each mode is a `PaletteProvider` that matches on prefix, searches, and executes results.

```java
PaletteProvider
├── matches(String rawInput)
├── search(String query) → List<PaletteResult>
└── execute(PaletteResult item)

PaletteResult
├── icon: Node
├── title: String
├── subtitle: String
└── action: Runnable
```

## Data Model

```java
Note
├── path: Path              // absolute path to .md file
├── title: String           // filename without .md, or first # heading
├── content: String         // raw markdown
├── lastModified: Instant
└── links: Set<String>      // outgoing [[wikilink]] targets

Vault
├── rootPath: Path
├── name: String            // folder name
└── notes: Map<String, Note>  // lowercase title → Note

Link
├── source: Path
├── target: String          // wikilink text
├── lineNumber: int
└── context: String         // surrounding text snippet

VaultConfig (persisted as ~/.config/orgx/config.json)
├── vaults: List<VaultEntry>  // name + path pairs
├── lastOpenedVault: String
├── lastOpenedNote: Path
├── showStatusBar: boolean
├── theme: String             // "light" / "dark"
└── sidebarStates              // visibility, split positions
```

## Markdown Parsing & WYSIWYG Rendering

**Two-way pipeline:**

```
              commonmark-java                 ModelBuilder
Markdown (.md) ──────────────► commonmark AST ──────────► RichTextModel
```

```
                          ModelSerializer
RichTextModel ──────────────────────────► Markdown (.md)
```

**Parser:** commonmark-java with a custom `WikiLinkExtension` to recognize `[[...]]` syntax. A `Visitor` walks the commonmark AST and builds the `RichTextModel` with appropriate `StyleAttributeMap` entries.

**Style mappings:**

| Markdown | StyleAttributeMap |
|---|---|
| `# Heading 1` | bold, fontSize 28 |
| `**bold**` | bold |
| `*italic*` | italic |
| `` `code` `` | monospace font, background color |
| `[[wikilink]]` | colored text, custom WIKILINK_TARGET attribute |
| `> blockquote` | spaceLeft indent, italic, border color |
| `- list item` | bullet attribute |
| code block | monospace, paragraph background |

**WYSIWYG behavior:** The model always contains the full markdown source. Rendering hides syntax markers visually rather than removing them from the model. This is achieved through styling — syntax markers (`**`, `#`, `[[`, `]]`, etc.) are styled with zero-width or transparent/dimmed text. When the caret enters a formatted region (defined as the current paragraph for block elements, or the current inline span for inline elements), the syntax markers in that region are restyled to be visible for editing.

This approach avoids model mutations on caret movement, preserving undo/redo integrity and keeping character offsets stable. The caret position listener triggers a restyle pass on the affected paragraph only.

**Round-trip fidelity:** The source markdown is preserved as the canonical representation. The model is rebuilt from source on open, and serialization writes back the source with any edits applied. This means the serializer does not need to "guess" markdown from styles — it tracks the original markdown structure and applies delta edits.

**Serializer:** Maintains a mapping between model segments and their source markdown ranges. On save, applies edits (insertions, deletions, formatting changes) to the original markdown text. New formatted text created by the user (e.g., making text bold via a shortcut) generates the appropriate markdown markers (`**`). Nested styles produce nested markers (`***bold+italic***`).

**Custom style attributes:**

```java
StyleAttribute<String> WIKILINK_TARGET = new StyleAttribute<>("WIKILINK_TARGET", String.class, true);
StyleAttribute<Integer> HEADING_LEVEL = new StyleAttribute<>("HEADING_LEVEL", Integer.class, true);
StyleAttribute<Boolean> CODE_BLOCK = new StyleAttribute<>("CODE_BLOCK", Boolean.class, true);
```

**Supported markdown subset (MVP):** Headings (1-6), bold, italic, inline code, code blocks, bullet lists, ordered lists, blockquotes, horizontal rules, links, images, `[[wikilinks]]`.

**Images:** Loaded from relative paths within the vault (e.g., `![](images/photo.png)` resolves relative to the note's directory). Rendered as inline `ImageView` nodes embedded in the RichTextArea. Missing images show a placeholder with the path.

## Event Bus & Core Services

**Event Bus:** Simple pub/sub. Components publish events, interested parties subscribe.

```java
EventBus
├── publish(Event event)
├── subscribe(Class<T> type, Consumer<T> handler)
└── unsubscribe(Class<T> type, Consumer<T> handler)
```

**Events:**

- `VaultOpened(Vault)`, `VaultClosed()`
- `NoteOpened(Note)`, `NoteSaved(Note)`, `NoteCreated(Path)`, `NoteDeleted(Path)`, `NoteRenamed(Path, Path)`, `NoteExternallyChanged(Path)`
- `LinkClicked(String target)`
- `SearchRequested(String query)`, `CommandPaletteOpened()`, `CommandPaletteClosed()`
- `ThemeChanged(String)`, `SettingChanged(String, Object)`

**Services:**

```
VaultManager — vault lifecycle, note CRUD, file scanning
LinkIndex — link graph: outgoing links, backlinks, link resolution
SearchEngine — full-text search with title fuzzy match and content search with context snippets
NoteEditor (service) — open/save notes, dirty tracking, markdown ↔ model conversion (business logic, no UI)
ConfigManager — load/save app config from ~/.config/orgx/config.json
FileWatcher — WatchService on vault directory, fires NoteExternallyChanged events
NavigationHistory — back/forward note history stack
```

**NoteEditor vs NoteEditorPanel:** `NoteEditor` is the service that holds business logic (open, save, dirty tracking, markdown conversion). `NoteEditorPanel` is the JavaFX `Node` that wraps the `RichTextArea` and handles UI concerns (caret-reveal styling, click handlers, scroll behavior). The panel delegates to the service for all data operations.

**AppShell:** The root `BorderPane` that assembles the layout — places the `SplitPane` in center, `StatusBar` in bottom, and manages sidebar visibility toggling.

**AppContext** creates all services at startup, wires them with the EventBus.

**Example flow — user clicks a wikilink:**

1. Editor detects click on wikilink segment
2. Publishes `LinkClicked("My Note")`
3. VaultManager resolves to Note, publishes `NoteOpened(note)`
4. Editor parses markdown, loads into RichTextArea
5. BacklinksPanel queries LinkIndex, refreshes list
6. FileTree highlights the file

## File Tree Panel

`TreeView<Path>` mirroring the vault directory. Only `.md` files shown. Folders first, then alphabetical.

**Context menu:** New Note, New Folder, Rename, Delete (with confirmation), Reveal in File Manager.

**Inline rename:** Double-click to rename in place.

**Event integration:** Clicking a file fires `NoteOpened`. Listens for `NoteCreated`, `NoteDeleted`, `NoteRenamed`, `NoteExternallyChanged` to stay in sync.

## Backlinks Panel

Right sidebar. Scrollable list of cards, each showing:

- **Note title** (clickable — opens that note)
- **Context snippet** — the line containing the `[[wikilink]]`, with the link highlighted

Subscribes to `NoteOpened` to refresh. Subscribes to `NoteSaved` to update if backlinks changed. Empty state shows "No backlinks" message.

## Keyboard Shortcuts

**Global:**

| Shortcut | Action |
|---|---|
| `Ctrl+P` | Open command palette |
| `Ctrl+B` | Toggle file tree |
| `Ctrl+Shift+B` | Toggle backlinks panel |
| `Ctrl+N` | New note |
| `Ctrl+S` | Save current note |
| `Ctrl+Z` / `Ctrl+Shift+Z` | Undo / Redo |
| `Ctrl+W` | Close current note |
| `Alt+Left` / `Alt+Right` | Navigate back / forward in history |
| `Ctrl+Shift+F` | Content search (palette with `?` prefix) |

**Editor:**

- Click on `[[wikilink]]` opens linked note
- Clicking a non-existent `[[wikilink]]` creates the note and opens it

## Theming

Two themes: **Light** (default) and **Dark**. Switchable via command palette or persisted setting.

```
src/main/resources/themes/
├── light.css
└── dark.css
```

Switching replaces the stylesheet on the scene. Tokens cover: backgrounds, foregrounds, selection, wikilink color, code backgrounds, heading styles, card styling, scrollbars.

## Module System & Dependencies

**Dependencies:**

- JavaFX 26 (`javafx-controls`)
- Log4j2 (`log4j-api`, `log4j-core`) 2.25.3
- commonmark-java (`commonmark`) 0.24.0
- JUnit 5 (test scope)

**No other external dependencies.** File watching, search indexing, event bus — all built with standard Java APIs.

**Search engine details:** In-memory inverted index built on vault open. Each note's content is tokenized and indexed. Title search uses substring matching with recently-opened notes boosted to the top. Content search scans the index and returns matching lines with surrounding context. Sufficient for vaults up to ~10k notes. No external search library needed.

**Config persistence:** `VaultConfig` is serialized as JSON using a simple hand-written serializer (the config structure is flat — no need for a JSON library). Alternatively, `java.util.Properties` can be used if JSON proves cumbersome without a library.

**Dirty state UX:** A modified note shows a dot indicator in the window title or file tree (e.g., "My Note *"). Closing a dirty note or switching vaults prompts a save dialog (Save / Discard / Cancel). Closing the app with dirty notes also prompts.

**FileWatcher limitation:** Linux inotify has a per-user watch limit (default ~8192). For very large vaults with many subdirectories, the watcher may need to fall back to periodic polling. For MVP, the WatchService approach is sufficient.

## Package Structure

```
src/main/java/app/orgx/desktop/
├── core/
│   ├── EventBus.java
│   ├── AppContext.java
│   └── events/              // event record classes
├── model/
│   ├── Note.java
│   ├── Vault.java
│   ├── Link.java
│   └── VaultConfig.java
├── vault/
│   ├── VaultManager.java
│   ├── LinkIndex.java
│   ├── FileWatcher.java
│   └── NavigationHistory.java
├── markdown/
│   ├── MarkdownToModel.java
│   ├── ModelToMarkdown.java
│   └── WikiLinkExtension.java
├── editor/
│   ├── NoteEditor.java
│   └── NoteEditorPanel.java
├── search/
│   └── SearchEngine.java
├── ui/
│   ├── AppShell.java
│   ├── FileTreePanel.java
│   ├── BacklinksPanel.java
│   ├── StatusBar.java
│   ├── CommandPalette.java
│   └── palette/
│       ├── PaletteProvider.java
│       ├── PaletteResult.java
│       ├── FileSearchProvider.java
│       ├── ContentSearchProvider.java
│       ├── CommandProvider.java
│       └── VaultSwitchProvider.java
├── config/
│   └── ConfigManager.java
├── Launcher.java
└── OrgxApplication.java
```
