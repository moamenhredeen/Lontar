# Orgx — Note-Taking App Design Spec

## Overview

A document-centric, Obsidian-like note-taking app built with JavaFX 26 and the new RichTextArea control. Notes are stored as plain markdown files on disk. The app provides WYSIWYG markdown editing, `[[wikilinks]]` with backlinks, full-text search, multi-vault support, and a command palette as the primary interaction hub.

## Paradigm & Storage

- **Document-centric:** Each note is a standalone `.md` file in a vault folder.
- **Markdown on disk:** Plain `.md` files, editable outside the app. No hidden databases or metadata folders.
- **Link resolution:** `[[My Note]]` resolves to `My Note.md` anywhere in the vault by case-insensitive filename match. Ambiguous matches prompt the user.
- **Multi-vault:** Users can open and switch between multiple vaults, IntelliJ-style (vault selector in command palette).

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

**WYSIWYG behavior:** Markdown syntax markers are hidden in rendered view. When the caret enters a formatted region, the raw markdown syntax is revealed for editing. When the caret leaves, it collapses back to rendered form. Implemented by listening to `caretPositionProperty()` and toggling segments between rendered and raw states.

**Serializer:** Walks `RichTextModel` segments and emits markdown based on style attributes (bold → `**`, heading → `#`, etc.).

**Custom style attributes:**

```java
StyleAttribute<String> WIKILINK_TARGET = new StyleAttribute<>("WIKILINK_TARGET", String.class, true);
StyleAttribute<Integer> HEADING_LEVEL = new StyleAttribute<>("HEADING_LEVEL", Integer.class, true);
StyleAttribute<Boolean> CODE_BLOCK = new StyleAttribute<>("CODE_BLOCK", Boolean.class, true);
```

**Supported markdown subset (MVP):** Headings (1-6), bold, italic, inline code, code blocks, bullet lists, ordered lists, blockquotes, horizontal rules, links, images, `[[wikilinks]]`.

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
- `SearchRequested(String query)`, `CommandPaletteOpened()`
- `ThemeChanged(String)`, `SettingChanged(String, Object)`

**Services:**

```
VaultManager — vault lifecycle, note CRUD, file scanning
LinkIndex — link graph: outgoing links, backlinks, link resolution
SearchEngine — full-text search with title fuzzy match and content search with context snippets
NoteEditor — open/save notes, dirty tracking, markdown ↔ model conversion
ConfigManager — load/save app config from ~/.config/orgx/config.json
FileWatcher — WatchService on vault directory, fires NoteExternallyChanged events
NavigationHistory — back/forward note history stack
```

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
