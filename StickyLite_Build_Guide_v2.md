# StickyLite Build Guide v2 (no-refactor order)

This replaces the old roadmap. The order changed so that **you never go back and rewrite a class you already finished.**

## The 4 rules that make that possible

1. **Data first, screens last.** Model, storage and controller are built before any window.
2. **Contracts before code.** Every class has its final file path and method signatures written here. Don't rename them while building.
3. **Components first, assembly last.** Each UI piece (top bar, text editor, drawing layer...) is built and tested alone in a scratch window. `NoteWindow` is assembled only after all pieces exist.
4. **Interfaces break circular dependencies.** `NoteWindow` needs `WindowManager`, and `WindowManager` creates `NoteWindow`. A small interface (`WindowActions`) built early solves that.

---

## How every step is written

| Part | Meaning |
|---|---|
| **File** | Exact path and type (class, enum, interface) |
| **What it is** | One-line description |
| **Why now** | Why it sits at this point in the order |
| **How it works** | The concept you need, in plain words |
| **Build it** | The exact fields and methods (the contract) |
| **Test** | How to prove it works before moving on |
| **Tutor prompt** | Copy it into any LLM. Copy the text only, without the `>` symbol |

### Creating files in IntelliJ

Right-click the package (e.g. `model`) > **New** > **Java Class**. In the popup, pick **Class**, **Enum** or **Interface** as each step says. IntelliJ creates the file in the right folder and adds the `package` line for you.

Generate getters and setters: click inside the class, press **Alt+Insert**, choose *Getter and Setter*, select fields. For interfaces you implement: **Alt+Enter** on the red underline > *Implement methods*.

### Package name

I write `com.stickynotev2` (Java convention is lowercase). If you keep `com.StickyNoteV2`, just swap it everywhere. To rename later: right-click the package > Refactor > Rename. I also use your spelling, **`NoteColour`**.

### Paste this ONCE at the start of every LLM chat

> You are my Java programming tutor. I am building a Windows sticky note desktop app called StickyLite with Java 17, Swing, FlatLaf, Gson and Maven in IntelliJ. Base package: com.stickynotev2 with sub-packages model, storage, controller, ui, util. Classes (final names): model.NoteColour, model.TextRun, model.InkStroke, model.Note, model.AppState, storage.NoteRepository, controller.NoteController, controller.WindowActions, controller.WindowManager, util.ScreenUtil, util.SingleInstanceLock, util.TextAttrs, util.TextRunConverter, util.ChecklistSupport, ui.TopBarListener, ui.BottomBarListener, ui.DrawMode, ui.TopBar, ui.BottomBar, ui.FormatToolbar, ui.RichTextArea, ui.DrawingLayer, ui.ThemePalette, ui.NoteWindow, ui.ManagerWindow, ui.TrayManager.
>
> RULES:
>
> 1. Never write the full class for me. Explain concepts, name the APIs I should look up, and show at most a 3-5 line example snippet from a different domain.
> 2. Before explaining, ask me one question about how I would approach it.
> 3. When I paste my code, point out problems with guiding questions instead of rewriting it.
> 4. Keep answers short. I have a limited token budget.
> 5. Only if I say "I'm stuck, show me" may you give a larger snippet.
> 6. Keep the exact class names, packages and method signatures I give you.

### Git

After every step that works: `git add . && git commit -m "step X.Y"`.

---

## Package and class layout (final)

```
src/main/java/com/stickynotev2
├── Main.java
├── Sandbox.java                 (scratch tester, deleted at the end)
├── model/
│   ├── NoteColour.java
│   ├── TextRun.java
│   ├── InkStroke.java
│   ├── Note.java
│   └── AppState.java
├── storage/
│   └── NoteRepository.java
├── controller/
│   ├── NoteController.java
│   ├── WindowActions.java
│   └── WindowManager.java
├── util/
│   ├── ScreenUtil.java
│   ├── SingleInstanceLock.java
│   ├── TextAttrs.java
│   ├── TextRunConverter.java
│   └── ChecklistSupport.java
└── ui/
    ├── TopBarListener.java
    ├── BottomBarListener.java
    ├── DrawMode.java
    ├── TopBar.java
    ├── BottomBar.java
    ├── FormatToolbar.java
    ├── RichTextArea.java
    ├── DrawingLayer.java
    ├── ThemePalette.java
    ├── NoteWindow.java
    ├── ManagerWindow.java
    └── TrayManager.java
```

Dependency direction: `ui` can use `controller`, `model`, `util`. `controller` can use `storage` and `model`. `model` uses nothing from the others. If you ever import "upwards", something is wrong.

---

# PHASE 0: Project setup

### Step 0.1: Maven project, libraries, packages

**File:** `pom.xml` (project root) and `src/main/java/com/stickynotev2/Main.java`

**Why now:** Everything else needs the libraries and the folders.

**How it works:** Maven reads `pom.xml`, downloads the libraries you list into a local cache, and puts them on your classpath. Packages are just folders; you split by responsibility so wrong imports are easy to spot.

**Build it**
1. IntelliJ: *New Project* > **Maven** (not an archetype), name `StickyNoteV2`, JDK 17 or newer.
2. In `pom.xml` set Java 17 and add two dependencies (look up the newest versions on search.maven.org; the numbers below are examples):

```xml
<properties>
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
<dependencies>
  <dependency>
    <groupId>com.formdev</groupId><artifactId>flatlaf</artifactId><version>3.5.4</version>
  </dependency>
  <dependency>
    <groupId>com.google.code.gson</groupId><artifactId>gson</artifactId><version>2.11.0</version>
  </dependency>
</dependencies>
```

3. Click the Maven refresh button (small "m" icon) so IntelliJ downloads them.
4. Create the 5 packages under `src/main/java` (right-click > New > Package): `com.stickynotev2.model`, `.storage`, `.controller`, `.ui`, `.util`.
5. Create `Main` with a `main` method. Inside, call `SwingUtilities.invokeLater(...)`; in that, call `FlatLightLaf.setup()` and show an empty `JFrame`.
6. VCS > Create Git Repository. Add a `.gitignore` containing `target/` and `.idea/`.

**Why `invokeLater`:** Swing is not thread-safe. All UI work must happen on one thread, the Event Dispatch Thread. `invokeLater` puts your code there.

**Test:** Run `Main` (green arrow next to `main`). A modern-looking empty window appears.

**Tutor prompt**

> STEP 0.1. I am creating a Maven project in IntelliJ for a Swing app. Explain: what pom.xml does, what a Maven dependency is and what "reload" does, and why Swing code must run inside SwingUtilities.invokeLater (Event Dispatch Thread). Ask me first what I think happens if I update a Swing component from the main thread. Don't write my Main class.

### Step 0.2: `Sandbox` (scratch tester)

**File:** `src/main/java/com/stickynotev2/Sandbox.java` (class with its own `main`)

**Why now:** In the UI phase you will build 8 components. Each needs a quick place to be shown alone. Having one scratch class stops you from touching `Main`.

**How it works:** IntelliJ lets you run any class that has a `main` method. `Sandbox.main` will create a `JFrame`, add whatever component you are testing, and show it. You edit it for every test. You delete it in Step 9.5.

**Build it:** `main` uses `invokeLater`, sets FlatLaf, creates a `JFrame` (size 400x400, `EXIT_ON_CLOSE`), and shows it.

**Test:** Run `Sandbox`; an empty window appears.

**Tutor prompt**

> STEP 0.2. In IntelliJ I want a second class with its own main method that I use only to display and test Swing components one at a time. Explain how IntelliJ handles multiple main classes and run configurations. Ask me what I want in the test window before suggesting anything. Don't write it.

---

# PHASE 1: Model (plain data, no windows)

**Rule for this whole phase:** No `javax.swing` imports. Only `NoteColour` may import `java.awt.Color`.

### Step 1.1: `NoteColour`

**File:** `model/NoteColour.java` (**Enum**)

**What it is:** The 7 note themes.

**Why now:** `Note` stores a colour, and every UI class paints with it. It has no dependencies, so it's the safest class to write first.

**How it works:** A Java enum is a fixed list of objects. Each can hold data. Syntax: constants first with constructor arguments, then a semicolon, then fields and a private constructor. Gson saves an enum as its name (`"YELLOW"`), so you never store colour objects in the file.

**Build it**
- Constants: `YELLOW, GREEN, BLUE, PURPLE, PINK, ORANGE, GRAY`.
- Fields: `String bodyHex`, `String headerHex`.
- Methods: `Color getBody()`, `Color getHeader()` (use `Color.decode(hex)`).

| Name | bodyHex | headerHex |
|---|---|---|
| YELLOW | #FFF59D | #FDD835 |
| GREEN | #C5E1A5 | #9CCC65 |
| BLUE | #90CAF9 | #42A5F5 |
| PURPLE | #CE93D8 | #AB47BC |
| PINK | #F8BBD0 | #F06292 |
| ORANGE | #FFCC80 | #FFA726 |
| GRAY | #E0E0E0 | #9E9E9E |

**Test:** In `Main`, print `NoteColour.GREEN.getBody()`. You should see `java.awt.Color[r=197,g=225,b=165]`.

**Tutor prompt**

> STEP 1.1. File: model/NoteColour.java, a Java enum. Spec: constants YELLOW, GREEN, BLUE, PURPLE, PINK, ORANGE, GRAY; each has String bodyHex and String headerHex; methods Color getBody() and Color getHeader() using Color.decode. Teach me: enum syntax with fields and a private constructor, why the semicolon after the constants, and how Color.decode works. Use a different example (planets). Then ask me to write it and review it.

### Step 1.2: `TextRun`

**File:** `model/TextRun.java` (**Class**)

**What it is:** A piece of text with one style. A note's text is a list of these.

**Why now:** `Note` needs it. Defining it now, with checkbox fields included, means you never change `Note` or the saved file format when you add formatting and checklists later.

**How it works:** Styled text in Swing is characters with attributes. To save it, you group neighbouring characters that share the same attributes into one "run". Example: "Hello **big** world" becomes 3 runs: `Hello `, `big` (bold), ` world`.

**Build it** (private fields, getters and setters, a no-argument constructor, and a convenience constructor `TextRun(String text)`)
- `String text` (default `""`)
- `boolean bold, italic, underline, strike`
- `int size` (`0` means "default size", do not store a number)
- `String font` (`null` means "default font")
- `boolean checkbox` (this run is a checkbox character) and `boolean checked`

**Test:** Create a run in `Main`, set bold, print it.

**Tutor prompt**

> STEP 1.2. File: model/TextRun.java, a plain Java class. It represents one run of identically styled text so a rich-text note can be saved as JSON. Fields (private, with getters/setters and a no-arg constructor): String text; boolean bold, italic, underline, strike; int size (0 = default); String font (null = default); boolean checkbox, checked. Explain what a "run" is in a styled document and why 0/null are used as "default". Ask me why storing the actual default size might cause trouble later. Don't write the class.

### Step 1.3: `InkStroke`

**File:** `model/InkStroke.java` (**Class**)

**What it is:** One pen stroke the user drew.

**Why now:** `Note` needs a list of them. Strokes are stored as **points** (vector), not pixels, so they save small, erase individually and stay sharp.

**Build it** (private fields, getters and setters, no-arg constructor)
- `List<int[]> points` (each `int[]` is `{x, y}`)
- `String colourHex` (e.g. `"#D32F2F"`)
- `int width`
- `boolean behindText`
- Method `void addPoint(int x, int y)`

**Naming note:** Java already has `java.awt.Stroke`, so yours is `InkStroke` to avoid clashes.

**Test:** Create a stroke, add 3 points, print `points.size()`.

**Tutor prompt**

> STEP 1.3. File: model/InkStroke.java, a plain Java class for a freehand pen stroke. Fields: List<int[]> points ({x,y} pairs), String colourHex, int width, boolean behindText; method addPoint(int x, int y). Explain vector vs bitmap storage for drawings and why the class is named InkStroke (java.awt.Stroke exists). Ask me how many bytes a bitmap of a 300x300 note would be vs 200 points, then quiz me on initialising the list so it is never null.

### Step 1.4: `Note`

**File:** `model/Note.java` (**Class**)

**What it is:** Everything saved about one note.

**Why now:** All later classes (controller, windows, manager) are built around it. All fields for the whole project are included **now** (including soft delete and strokes), so it never changes again.

**How it works:** A window is temporary; a note must survive restarts. So the note is plain data and the window just *displays* it. Gson saves private fields directly through reflection, so field names become JSON keys.

**Build it** (private fields, getters and setters via Alt+Insert, a no-arg constructor)

| Field | Type | Default for a new note |
|---|---|---|
| `id` | String | `UUID.randomUUID().toString()` |
| `content` | `List<TextRun>` | empty list |
| `strokes` | `List<InkStroke>` | empty list |
| `colour` | `NoteColour` | `YELLOW` |
| `x, y, width, height` | int | 200, 150, 300, 320 |
| `open` | boolean | `true` |
| `pinned` | boolean | `false` |
| `deleted` | boolean | `false` |
| `createdAt, updatedAt, deletedAt` | long | `System.currentTimeMillis()`, same, `0` |

Methods:
- `static Note createNew(int x, int y)` returns a new note at that position.
- `String getPlainText()` joins all `content` run texts (needed for manager previews and search).

Boolean field `open` generates `isOpen()` / `setOpen(...)`.

**Test:** `Note.createNew(10, 10)`, add a run `"hello"`, print `getPlainText()`.

**Tutor prompt**

> STEP 1.4. File: model/Note.java, plain data class with no Swing. Fields: String id (UUID), List<TextRun> content, List<InkStroke> strokes, NoteColour colour (default YELLOW), int x, y, width, height (defaults 200,150,300,320), boolean open (true), pinned, deleted, long createdAt, updatedAt, deletedAt. Methods: static Note createNew(int x, int y), String getPlainText(). Explain why the data model is separate from the window, what UUID is, how IntelliJ generates getters/setters for boolean fields, and why lists must never be null. Ask me which fields change often. Don't write the class.

### Step 1.5: `AppState`

**File:** `model/AppState.java` (**Class**)

**What it is:** The wrapper object that becomes the whole JSON file.

**Why now:** The repository saves and loads exactly one object. A wrapper lets you add settings later without changing the file layout.

**Build it:** `int version = 1;` and `List<Note> notes = new ArrayList<>();` with getters.

**Test:** Create an `AppState`, add 2 notes, print the size.

**Tutor prompt**

> STEP 1.5. File: model/AppState.java. It is the root object saved to JSON: int version (1) and List<Note> notes (initialised). Explain why wrapping the list in a root object is better than saving a bare list (future settings, versioning). Ask me what I would add later. Don't write the class.

---

# PHASE 2: Storage

### Step 2.1: `NoteRepository`

**File:** `storage/NoteRepository.java` (**Class**)

**What it is:** The only class that touches the disk.

**Why now:** The controller (next phase) needs it. Because only this class knows about files, you could later swap JSON for a database by changing one class.

**How it works**
- **File location:** `%APPDATA%\StickyLite\notes.json` (get the folder with `System.getenv("APPDATA")`; if it is `null`, use `System.getProperty("user.home")`).
- **File format:** JSON, written by Gson with pretty printing. Example:

```json
{
  "version": 1,
  "notes": [
    {
      "id": "6f1c...",
      "content": [
        { "text": "Shopping\n", "bold": true, "size": 18 },
        { "text": "☐", "checkbox": true, "checked": false },
        { "text": " Milk\n" }
      ],
      "strokes": [
        { "points": [[10,10],[12,14]], "colourHex": "#D32F2F", "width": 3, "behindText": false }
      ],
      "colour": "YELLOW",
      "x": 200, "y": 150, "width": 300, "height": 320,
      "open": true, "pinned": false, "deleted": false,
      "createdAt": 1730000000000, "updatedAt": 1730000000000, "deletedAt": 0
    }
  ]
}
```

- **Safe saving:** Write to `notes.json.tmp` first, then move it over `notes.json`. If the app dies mid-write, the old file is still intact.
- **Encoding:** Always UTF-8, or the ☐ checkbox characters will break.

**Build it**
- Constructor `NoteRepository(Path file)` and `static Path defaultPath()` (so tests can use a temp file).
- `AppState load()`: file missing returns a new empty `AppState`. File unreadable or invalid JSON: copy it to `notes.json.bak`, then return an empty `AppState` (never crash on startup).
- `void save(AppState state)`: create the folder if missing, write temp file, move into place.

**Hints:** `Files.createDirectories`, `Files.writeString(path, text, StandardCharsets.UTF_8)`, `Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)`, `new GsonBuilder().setPrettyPrinting().create()`, `gson.toJson(obj)` / `gson.fromJson(text, AppState.class)`. `fromJson` can return `null` for an empty file; handle it.

**Test:** In `Main`: build an `AppState` with 2 notes, `save`, then `load` and print note IDs. Open the JSON file in Notepad and read it. Then break the JSON by hand and confirm a `.bak` file appears and the program does not crash.

**Tutor prompt**

> STEP 2.1. File: storage/NoteRepository.java. Spec: constructor NoteRepository(Path file); static Path defaultPath() = %APPDATA%/StickyLite/notes.json (fallback user.home); AppState load() (missing file returns empty AppState; corrupt file is copied to notes.json.bak then returns empty); void save(AppState) writing UTF-8 pretty JSON via Gson to notes.json.tmp then moving over notes.json. Already built: model classes Note, TextRun, InkStroke, NoteColour, AppState. Teach me: Gson toJson/fromJson with nested lists and enums, java.nio.file Files/Path basics, why write-temp-then-move is safer, and why I must set UTF-8. Ask me what could go wrong if the app crashes mid-save. Don't write the class.

---

# PHASE 3: Controller

### Step 3.1: `NoteController`

**File:** `controller/NoteController.java` (**Class**)

**What it is:** The one place that owns the notes in memory and decides when to save.

**Why now:** Every window and the manager will talk to it. Building it now, with delete, restore, search and change notifications already included, means the windows never need rewiring later.

**How it works**
- It loads an `AppState` once at construction and keeps it in memory. Windows edit `Note` objects directly, then tell the controller "this one changed".
- **Debounced save:** saving on every keystroke is wasteful. Use a `javax.swing.Timer` (500 ms, `setRepeats(false)`). Each change calls `timer.restart()`; the save happens only after 500 ms of quiet.
- **Change listeners:** a list of `Runnable`s called after any change, so the manager window can refresh itself.
- **Soft delete:** delete only sets `deleted = true`, so the user can restore.

**Build it**
- `NoteController(NoteRepository repo)`
- `Note createNote()` and `Note createNote(int x, int y)`
- `Note getNote(String id)`
- `List<Note> getAllNotes()` (not deleted, most recently edited first)
- `List<Note> getOpenNotes()` (not deleted and `open`)
- `List<Note> getDeletedNotes()`
- `List<Note> search(String query)` (case-insensitive match on plain text; empty query returns all)
- `void updateNote(Note n, boolean contentChanged)`: if `contentChanged` set `updatedAt` to now; schedule save; notify listeners. (Moving or resizing a window is not a content change.)
- `void softDeleteNote(String id)`: `deleted = true`, `deletedAt = now`, `open = false`
- `void restoreNote(String id)`
- `void purgeOldTrash(int days)`: permanently remove notes deleted more than `days` ago
- `void saveNow()`: stop the timer and save immediately
- `void addChangeListener(Runnable r)`

**Test:** In `Main`: create 3 notes, soft-delete one, call `saveNow()`, restart the program and check `getAllNotes().size() == 2`, `getDeletedNotes().size() == 1`. Call `updateNote` 20 times quickly and add a `System.out.println` in the save to confirm it runs once.

**Tutor prompt**

> STEP 3.1. File: controller/NoteController.java. Already built: model (Note, TextRun, InkStroke, NoteColour, AppState) and storage.NoteRepository (load(), save(AppState)). Spec: constructor NoteController(NoteRepository); createNote() and createNote(int x,int y); getNote(id); getAllNotes() (not deleted, newest edited first); getOpenNotes(); getDeletedNotes(); search(String); updateNote(Note, boolean contentChanged) which sets updatedAt only if contentChanged, schedules a debounced save (javax.swing.Timer 500ms, non-repeating, restart on each call) and notifies listeners; softDeleteNote(id); restoreNote(id); purgeOldTrash(int days); saveNow(); addChangeListener(Runnable). Teach me: MVC roles, debouncing, javax.swing.Timer vs java.util.Timer, the observer pattern with a List<Runnable>, soft delete. Ask me which methods change data and so must trigger a save. Don't write the class.

---

# PHASE 4: Utilities

### Step 4.1: `ScreenUtil`

**File:** `util/ScreenUtil.java` (**Class** with static methods)

**Why now:** `NoteWindow` (Phase 7) will call it when loading a position. If a monitor was unplugged, a note saved at x=2500 would open off-screen and be lost.

**How it works:** Ask Java for all screen rectangles; if the note's rectangle does not overlap any, move it to the primary screen.

**Build it:** `static Rectangle ensureVisible(Rectangle r, int minWidth, int minHeight)` which also enforces the minimum size.

**Hints:** `GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()`, `device.getDefaultConfiguration().getBounds()`, `Rectangle.intersects`.

**Test:** `ensureVisible(new Rectangle(9999, 9999, 10, 10), 200, 150)` returns a rectangle on your screen with at least 200x150.

**Tutor prompt**

> STEP 4.1. File: util/ScreenUtil.java, static utility. Spec: static Rectangle ensureVisible(Rectangle r, int minWidth, int minHeight) - enforce min size, and if the rectangle does not intersect any screen, move it onto the primary screen. Teach me: GraphicsEnvironment, GraphicsDevice, getDefaultConfiguration().getBounds(), Rectangle.intersects, multi-monitor coordinates (can be negative). Ask me what should happen for a note that is only partly visible. Don't write it.

### Step 4.2: `SingleInstanceLock`

**File:** `util/SingleInstanceLock.java` (**Class** with static methods)

**Why now:** `Main` (Phase 8) calls it first. Two copies would fight over one file.

**How it works:** Try to open a `ServerSocket` on a fixed local port (e.g. 45871) bound to `127.0.0.1`. The first copy succeeds and keeps listening. A second copy fails, so it connects to that port, sends the text `SHOW`, and exits. The first copy receives it and brings its notes to the front.

**Build it**
- `static boolean acquire(Runnable onSecondLaunch)`: returns `true` if this is the first copy (and starts a daemon thread accepting connections; on each connection call `onSecondLaunch` via `SwingUtilities.invokeLater`). Returns `false` if another copy exists (after sending `SHOW`).
- `static void release()`

**Test:** Run it twice from IntelliJ (allow parallel runs). Second one prints "already running" and quits.

**Tutor prompt**

> STEP 4.2. File: util/SingleInstanceLock.java, static utility. Spec: static boolean acquire(Runnable onSecondLaunch) - try binding a ServerSocket to 127.0.0.1 on a fixed port; on success start a daemon thread accepting connections and call onSecondLaunch on the Swing thread for each; on failure connect to the port, send "SHOW", return false. static void release(). Teach me: ServerSocket/Socket basics, daemon threads, why UI callbacks must use SwingUtilities.invokeLater. Ask me what happens if another program already uses that port. Don't write it.

### Step 4.3: `TextAttrs`

**File:** `util/TextAttrs.java` (**Class** with only constants)

**Why now:** The converter, the editor and the checklist code all use the same custom attribute names. One shared file prevents typos.

**Build it:** `public static final String CHECKBOX = "stickyCheckbox";`, `CHECKED = "stickyChecked";`, `public static final char BOX_EMPTY = '\u2610';`, `BOX_CHECKED = '\u2611';`, `DEFAULT_FONT = "Segoe UI"`, `DEFAULT_SIZE = 14`.

**How it works:** A Swing `AttributeSet` can hold any key and value, not only built-in styles. Keys are just objects, so strings work.

**Test:** Compiles.

**Tutor prompt**

> STEP 4.3. File: util/TextAttrs.java, constants only: String CHECKBOX, String CHECKED (custom AttributeSet keys), char BOX_EMPTY (U+2610), char BOX_CHECKED (U+2611), String DEFAULT_FONT, int DEFAULT_SIZE. Explain how Swing AttributeSet/SimpleAttributeSet can store custom keys beyond StyleConstants and why shared constants avoid bugs. Ask me why the checkbox state must be stored as an attribute and not only as the character. Short answer please.

---

# PHASE 5: Contracts (interfaces)

An **interface** is a promise: "any class that implements me will have these methods." Writing the promises first lets you build the top bar, bottom bar and windows independently, with no circular dependencies.

### Step 5.1: Three interfaces and one enum

**Files (create each via New > Java Class > Interface/Enum):**

`controller/WindowActions.java` (**Interface**)
```
void newNote();
void openManager();
void openNote(String id);
void closeNote(String id);      // hides the window, note stays saved
void deleteNote(String id);     // soft delete + closes window
```
`WindowManager` will implement this later. `NoteWindow` and `ManagerWindow` only know this interface.

`ui/TopBarListener.java` (**Interface**)
```
void onNewNote();
void onOpenManager();
void onClose();
void onPinToggled(boolean pinned);
void onColourClicked(java.awt.Component anchor);   // so the popup can appear under the button
void onDeleteRequested();
```

`ui/DrawMode.java` (**Enum**): `TEXT, PEN, ERASER`

`ui/BottomBarListener.java` (**Interface**)
```
void onFormatToggle();
void onChecklistClicked();
void onModeChanged(DrawMode mode);
void onPenColourChanged(java.awt.Color colour);
void onPenSizeChanged(int size);
void onBehindTextChanged(boolean behind);
void onClearDrawings();
```

**Why now:** Steps 6.1 and 6.6 need these to compile. Steps 7.x implement them.

**How it works:** The `TopBar` doesn't know who listens. `NoteWindow` will say `implements TopBarListener` and decide what each click does.

**Test:** Compiles.

**Tutor prompt**

> STEP 5.1. I am writing interfaces first so UI components can be built separately: WindowActions {newNote(); openManager(); openNote(String id); closeNote(String id); deleteNote(String id);}, TopBarListener {onNewNote(); onOpenManager(); onClose(); onPinToggled(boolean); onColourClicked(Component anchor); onDeleteRequested();}, enum DrawMode {TEXT, PEN, ERASER}, BottomBarListener {onFormatToggle(); onChecklistClicked(); onModeChanged(DrawMode); onPenColourChanged(Color); onPenSizeChanged(int); onBehindTextChanged(boolean); onClearDrawings();}. Teach me what a Java interface is, the listener/callback pattern, and how it avoids two classes depending on each other. Ask me who should implement WindowActions and why. Short answer.

---

# PHASE 6: UI components, one at a time

**How to test each:** edit `Sandbox.main` to put just that component in the test `JFrame`. Use anonymous listeners that print to the console.

### Step 6.1: `TopBar`

**File:** `ui/TopBar.java` (**Class** extending `JPanel`)

**Why now:** Needs only `TopBarListener` and `NoteColour`. It is fully independent.

**How it works:** Layout is `BorderLayout`: `WEST` holds the ☰ button; `CENTER` is an empty label (the drag area, so users can grab the note); `EAST` is a small `FlowLayout` panel with 🎨, 📌 (a `JToggleButton`), `+`, `✕`. Each button just calls the matching listener method. Right-click on the drag area shows a popup menu with "Delete note".

**Build it**
- Constructor `TopBar(TopBarListener listener)`
- `JComponent getDragHandle()` (the centre area, so `NoteWindow` can attach its drag code)
- `void applyColour(NoteColour colour)` (background = header colour)
- `void setPinned(boolean pinned)` (sets the toggle without firing the listener)
- Make buttons flat: `setBorderPainted(false)`, `setFocusPainted(false)`, `setContentAreaFilled(false)`.

**Hint:** Emoji may look rough in Swing. Plain text symbols (☰ + ✕ ★) are fine for now; real icons are a polish item.

**Test:** Show it in `Sandbox` with a listener printing each call. Click every button and right-click the middle.

**Tutor prompt**

> STEP 6.1. File: ui/TopBar.java extends JPanel. Already built: ui.TopBarListener, model.NoteColour. Spec: constructor TopBar(TopBarListener); BorderLayout - WEST: menu button (calls onOpenManager); CENTER: drag area; EAST: colour button (onColourClicked(this button)), pin JToggleButton (onPinToggled(selected)), + (onNewNote), x (onClose); right-click on the drag area opens a JPopupMenu with "Delete note" (onDeleteRequested). Methods: JComponent getDragHandle(); void applyColour(NoteColour) (background = header colour); void setPinned(boolean) without firing the listener. Teach me: BorderLayout/FlowLayout, making flat Swing buttons, JPopupMenu with a MouseListener (isPopupTrigger), JToggleButton. Ask me how I would avoid firing the listener inside setPinned. Don't write the class.

### Step 6.2: `TextRunConverter`

**File:** `util/TextRunConverter.java` (**Class** with static methods)

**Why now:** The editor (next) needs it to load and save formatted text. It depends only on `TextRun` and `TextAttrs`, so you test it alone with a plain document.

**How it works:** A `StyledDocument` is a tree. The root has *paragraph* elements; each paragraph has *leaf* elements, which are exactly "runs" of identical attributes. You loop them, read their attributes, and make `TextRun` objects. Going back, you insert each run's text with matching attributes.

Reading: `StyleConstants.isBold(attrs)`, `isItalic`, `isUnderline`, `isStrikeThrough`. For size and font use `attrs.isDefined(StyleConstants.FontSize)` first: `isDefined` checks only attributes set directly on that text, so unformatted text gives `0`/`null` and does not get the editor's default stored. Checkbox: `attrs.getAttribute(TextAttrs.CHECKBOX)`.

**Build it**
- `static List<TextRun> toRuns(StyledDocument doc)`: loop paragraphs, then leaves; text via `doc.getText(start, end - start)`. Merge neighbouring runs with identical styles.
- `static void fromRuns(List<TextRun> runs, StyledDocument doc)`: clear the document (`doc.remove(0, doc.getLength())`), then for each run build a `SimpleAttributeSet` (set only non-default properties) and `doc.insertString(doc.getLength(), run.getText(), attrs)`.
- Catch `BadLocationException` inside (wrap in `RuntimeException`).

**Test:** Make a `DefaultStyledDocument`, insert "Hello " plain and "big" bold, call `toRuns`, print: expect 2 runs. Then `fromRuns` into a new doc and `toRuns` again; the result must be identical.

**Tutor prompt**

> STEP 6.2. File: util/TextRunConverter.java, static utility. Already built: model.TextRun (text, bold, italic, underline, strike, int size (0=default), String font (null=default), checkbox, checked) and util.TextAttrs (CHECKBOX, CHECKED keys). Spec: static List<TextRun> toRuns(StyledDocument) walking the element tree (paragraph elements then leaf elements), reading attributes with StyleConstants, using attrs.isDefined(...) so default size/font stay 0/null, merging adjacent identical runs; static void fromRuns(List<TextRun>, StyledDocument) clearing the doc then inserting each run's text with a SimpleAttributeSet. Teach me: the Element tree of a StyledDocument, AttributeSet vs isDefined, Document.insertString and BadLocationException. Ask me how I would detect a run boundary. Don't write the class.

### Step 6.3: `RichTextArea`

**File:** `ui/RichTextArea.java` (**Class** extending `JTextPane`)

**Why now:** All text behaviour lives in one class: formatting, undo/redo, shortcuts and a "content changed" signal. `NoteWindow` just uses it.

**How it works**
- **Formatting a selection:** `setCharacterAttributes(attrs, false)` does the right thing: with a selection it styles it; with no selection it changes the style of text you type next.
- **Toggle logic:** read the current state (`getInputAttributes()` when no selection, otherwise the attributes at `getSelectionStart()` via `getStyledDocument().getCharacterElement(pos).getAttributes()`), then apply the opposite.
- **Defaults:** set the document's default style font and size to `TextAttrs.DEFAULT_FONT` and `DEFAULT_SIZE` so default text has a defined look.
- **Quiet loading:** a boolean `loading` flag is set while `setRuns` runs so the content listener does not fire and save needlessly.
- **Undo:** `UndoManager` attached with `doc.addUndoableEditListener(...)`. After `setRuns`, call `discardAllEdits()` so the user can't undo into an empty note.
- **Shortcuts:** use key bindings, not KeyListener: `getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control B"), "bold")` and `getActionMap().put("bold", new AbstractAction(){...})`.

**Build it**
- `void toggleBold()`, `toggleItalic()`, `toggleUnderline()`, `toggleStrikethrough()`
- `void setFontSize(int size)`, `void setFontFamily(String family)`
- `boolean isBoldAtCaret()`, `isItalicAtCaret()`, `isUnderlineAtCaret()`, `isStrikeAtCaret()`, `int getSizeAtCaret()`, `String getFontAtCaret()`
- `List<TextRun> getRuns()` and `void setRuns(List<TextRun> runs)` (use `TextRunConverter`)
- `void addContentListener(Runnable r)` (fires on any document change, not while `loading`)
- `void addStyleListener(Runnable r)` (fires when the caret moves, so the toolbar can update)
- `void undo()`, `void redo()`
- Shortcuts: Ctrl+B, I, U, Z, Y
- The component is see-through: `setOpaque(false)`

**Test:** In `Sandbox` inside a `JScrollPane`. Type, select, press Ctrl+B / Ctrl+I, Ctrl+Z / Ctrl+Y. Print `getRuns()` after formatting; call `setRuns(getRuns())` and confirm nothing changes visually.

**Tutor prompt**

> STEP 6.3. File: ui/RichTextArea.java extends JTextPane. Already built: util.TextRunConverter (toRuns/fromRuns on StyledDocument), util.TextAttrs (DEFAULT_FONT, DEFAULT_SIZE), model.TextRun. Spec methods: toggleBold/Italic/Underline/Strikethrough(); setFontSize(int); setFontFamily(String); isBoldAtCaret/isItalicAtCaret/isUnderlineAtCaret/isStrikeAtCaret(); getSizeAtCaret(); getFontAtCaret(); List<TextRun> getRuns(); void setRuns(List<TextRun>) (no content-listener firing while loading, then clear undo history); addContentListener(Runnable); addStyleListener(Runnable) (caret moves); undo(); redo(); key bindings Ctrl+B/I/U/Z/Y via InputMap/ActionMap; default style font/size set from TextAttrs; setOpaque(false). Teach me: StyledDocument, SimpleAttributeSet, StyleConstants, JTextPane.setCharacterAttributes with and without selection, input attributes, UndoManager, key bindings vs KeyListener. Ask me how toggle-bold should behave when half the selection is bold. Don't write the class.

### Step 6.4: `FormatToolbar`

**File:** `ui/FormatToolbar.java` (**Class** extending `JPanel`)

**Why now:** It only needs the finished `RichTextArea`.

**How it works:** It receives the editor in its constructor and wires every button itself, so `NoteWindow` never touches it beyond showing/hiding. It registers with `addStyleListener` and refreshes its buttons from `isBoldAtCaret()` etc., so the **B** button is highlighted when the caret is in bold text.

**Build it**
- Constructor `FormatToolbar(RichTextArea target)`
- Buttons **B**, *I*, <u>U</u>, ~~S~~ (`JToggleButton`s), font `JComboBox` (from `GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()`), size `JComboBox` (10, 12, 14, 16, 18, 24, 32)
- `void applyColour(NoteColour colour)`
- It starts hidden: `setVisible(false)`.
- When a toggle is clicked, call `target.requestFocusInWindow()` so typing continues.

**Test:** In `Sandbox`, `RichTextArea` in the centre and the toolbar above it, set visible. Click buttons with and without a selection. Move the caret into bold text and check **B** lights up.

**Tutor prompt**

> STEP 6.4. File: ui/FormatToolbar.java extends JPanel. Already built: ui.RichTextArea with toggleBold/Italic/Underline/Strikethrough(), setFontSize(int), setFontFamily(String), isBoldAtCaret() etc., getSizeAtCaret(), getFontAtCaret(), addStyleListener(Runnable). Spec: constructor FormatToolbar(RichTextArea target) wires everything itself; JToggleButtons B/I/U/S, a font JComboBox (all system font families), a size JComboBox; refreshes from the caret via target.addStyleListener; void applyColour(NoteColour); hidden by default; returns focus to the editor after each click. Teach me: JComboBox with listeners, GraphicsEnvironment font names, syncing UI state from a model without causing feedback loops. Ask me how I would avoid the combo box firing an action when I set it programmatically. Don't write the class.

### Step 6.5: `ChecklistSupport`

**File:** `util/ChecklistSupport.java` (**Class** with static methods)

**Why now:** It needs the finished `RichTextArea`, `TextAttrs` and the converter (checkbox state is already saved by the earlier steps).

**How it works: the key design idea.** A checkbox is **not a component**. It is a normal character (☐ or ☑) with a custom attribute `CHECKBOX = true`. Because it is a character, the arrow keys, selection, copy and editing already work, which gives you the "flush with the text" feel. Three behaviours:

1. **`[]` becomes a checkbox.** Install a `DocumentFilter` on the document (`((AbstractDocument) doc).setDocumentFilter(...)`). In `insertString`/`replace`, if the typed text is `]` and the text just before the caret is `[` or `[ `, remove that text and insert `☐` with attributes `CHECKBOX=true`, `CHECKED=false` instead. A filter runs *before* the change, so you are allowed to modify the document there (a plain `DocumentListener` would throw an exception if you tried).
2. **Click toggles.** Add a `MouseListener`. Convert the click to a document position with `area.viewToModel2D(e.getPoint())`. If the character there has the checkbox attribute, replace it with ☑/☐ (flip `CHECKED`) and apply strikethrough to the rest of that line when checked.
3. **Enter and Backspace.** Wrap the `ActionMap` entry for `DefaultEditorKit.insertBreakAction`: if the current line starts with a checkbox, insert a new line plus a new ☐; if the line has only a checkbox, remove it and end the list. Same idea for `DefaultEditorKit.deleteNextCharAction`/`deletePrevCharAction` when Backspace is pressed right after a checkbox.

**Build it**
- `static void install(RichTextArea area)` sets the filter, mouse listener and key actions
- `static void insertCheckbox(RichTextArea area)` inserts ☐ at the start of the current line (used by the bottom-bar button)

**Test:** In `Sandbox`: type `[]`, you get ☐. Click it: ☑ plus strikethrough. Press Enter on a checklist line, then Enter again on the empty one to leave the list. Save with `getRuns()`, reload with `setRuns`, and the checkboxes and their state survive. If the boxes show as empty squares, set the character's font to "Segoe UI Symbol".

**Tutor prompt**

> STEP 6.5. File: util/ChecklistSupport.java, static utility. Already built: ui.RichTextArea (extends JTextPane), util.TextAttrs (CHECKBOX, CHECKED, BOX_EMPTY U+2610, BOX_CHECKED U+2611). Design: a checkbox is a normal character with custom attributes, not a Swing component. Spec: static void install(RichTextArea) - (1) a DocumentFilter that turns typing "[]" or "[ ]" into a checkbox character, (2) a MouseListener using viewToModel2D that toggles a clicked checkbox and strikes through that line, (3) wrapped insertBreakAction so Enter continues the list and Enter on an empty item ends it, plus Backspace after a checkbox removes it; static void insertCheckbox(RichTextArea) inserts a checkbox at the start of the current line. Teach me: DocumentFilter vs DocumentListener (why I cannot modify the document inside a listener), viewToModel2D, wrapping an existing Action in ActionMap. Ask me how I would detect that a line is empty apart from its checkbox. Don't write the class.

### Step 6.6: `BottomBar`

**File:** `ui/BottomBar.java` (**Class** extending `JPanel`)

**Why now:** Needs only `BottomBarListener` and `DrawMode`.

**How it works:** Two rows, so it fits 300 px. Row 1 (always visible): ✔ checklist, **Aa** format toggle, three mode toggles **Text / Pen / Eraser** (a `ButtonGroup`, default Text), and a resize grip at the right. Row 2 (pen options, visible only in Pen or Eraser mode): colour swatches, size dropdown, "Behind text" checkbox, "Clear" button. Pen colour: a few preset swatch buttons (red, black, blue, green) is simpler than a full colour chooser.

**Build it**
- Constructor `BottomBar(BottomBarListener listener)`
- `JComponent getResizeGrip()` (a small label at the bottom-right with `Cursor.SE_RESIZE_CURSOR`)
- `void applyColour(NoteColour colour)`
- Each control calls the matching listener method. Showing and hiding row 2 happens inside `BottomBar` (call `revalidate()` and `repaint()`).

**Test:** In `Sandbox` with a printing listener. Switch modes and check row 2 appears and disappears.

**Tutor prompt**

> STEP 6.6. File: ui/BottomBar.java extends JPanel. Already built: ui.BottomBarListener {onFormatToggle(); onChecklistClicked(); onModeChanged(DrawMode); onPenColourChanged(Color); onPenSizeChanged(int); onBehindTextChanged(boolean); onClearDrawings();}, ui.DrawMode {TEXT, PEN, ERASER}, model.NoteColour. Spec: constructor BottomBar(BottomBarListener); row 1: checklist button, "Aa" button, Text/Pen/Eraser JToggleButtons in a ButtonGroup (default Text), and a resize grip label; row 2 (only visible in Pen/Eraser mode): colour swatch buttons, size JComboBox, "Behind text" JCheckBox, "Clear" button; methods JComponent getResizeGrip() and void applyColour(NoteColour). Teach me: BoxLayout or nested panels for two rows, ButtonGroup, showing/hiding a panel (revalidate/repaint). Ask me what to do so the swatch colour selection shows which one is active. Don't write the class.

### Step 6.7: `DrawingLayer`

**File:** `ui/DrawingLayer.java` (**Class** extending `JPanel`)

**Why now:** It depends only on `InkStroke` and `DrawMode`. It is built as a transparent surface that `NoteWindow` stacks in two layers (one behind the text, one in front).

**How it works: the important design.**
- Both layers share the **same** `List<InkStroke>` (the note's list). Each layer **paints** only strokes whose `behindText` matches its own setting.
- Only **one** layer, the front one, ever **receives mouse input**. A pen stroke made with "behind text" on is simply recorded with `behindText = true` and painted by the back layer. (The back layer cannot receive mouse events because the text editor sits above it and would block them.)
- To let clicks pass through to the text while in Text mode, override `contains(int x, int y)` to return `false` when the layer does not take input or the mode is `TEXT`. Swing then hands the click to whatever is underneath.
- Painting: `Graphics2D` with `RenderingHints.KEY_ANTIALIASING`, `BasicStroke(width, CAP_ROUND, JOIN_ROUND)`, `drawPolyline` (a one-point stroke is drawn as a dot).
- Eraser: on press/drag, remove any stroke in the shared list that has a point within 8 px of the cursor (compare squared distance), then ask for a redraw.

**Build it**
- Constructor `DrawingLayer(List<InkStroke> sharedStrokes, boolean paintsBehindText, boolean takesInput)`, `setOpaque(false)`
- `void setStrokes(List<InkStroke> strokes)` (swap the shared list when a note loads)
- `void setMode(DrawMode mode)`, `setPenColour(Color c)`, `setPenWidth(int w)`, `setNewStrokesBehindText(boolean b)`
- `void setOnRedrawNeeded(Runnable r)` (called while drawing and erasing, so `NoteWindow` repaints both layers)
- `void setOnStrokeFinished(Runnable r)` (called on mouse release, so `NoteWindow` saves)
- Cursor: crosshair in Pen and Eraser modes.

**Test:** In `Sandbox` with one layer (`paintsBehindText=false`, `takesInput=true`) over a text area. In Pen mode draw lines; in Text mode click through to the text. Test erase.

**Tutor prompt**

> STEP 6.7. File: ui/DrawingLayer.java extends JPanel (transparent). Already built: model.InkStroke (List<int[]> points, colourHex, width, behindText, addPoint), ui.DrawMode. Spec: constructor DrawingLayer(List<InkStroke> sharedStrokes, boolean paintsBehindText, boolean takesInput) with setOpaque(false); setStrokes(List); setMode(DrawMode); setPenColour(Color); setPenWidth(int); setNewStrokesBehindText(boolean); setOnRedrawNeeded(Runnable); setOnStrokeFinished(Runnable). Painting: Graphics2D antialiased, BasicStroke round caps, draws only strokes whose behindText == paintsBehindText. Input only when takesInput && mode != TEXT: press starts a new InkStroke added to the shared list, drag adds points, release calls the finished callback; ERASER removes strokes with a point within 8 px. Override contains(x,y) to return false when the layer shouldn't take mouse events so clicks pass to the component beneath. Teach me: paintComponent and Graphics2D, mouse listeners, why overriding contains() lets events fall through, and distance hit-testing. Ask me why only the front layer can receive input. Don't write the class.

### Step 6.8: `ThemePalette`

**File:** `ui/ThemePalette.java` (**Class** extending `JPopupMenu`)

**Why now:** It only needs `NoteColour`. `NoteWindow` will pass it a callback.

**How it works:** The popup contains a row of 7 round colour buttons. You make a small `JButton` subclass (or anonymous class) that overrides `paintComponent` to draw a filled circle in `colour.getBody()` with a darker outline. Clicking calls the callback and closes the popup.

**Build it**
- Constructor `ThemePalette(java.util.function.Consumer<NoteColour> onPick)`
- `void showBelow(Component anchor)` (use `show(anchor, 0, anchor.getHeight())`)

**Test:** In `Sandbox` a button that opens the palette; clicking a colour prints its name.

**Tutor prompt**

> STEP 6.8. File: ui/ThemePalette.java extends JPopupMenu. Already built: model.NoteColour (7 values with getBody()/getHeader()). Spec: constructor ThemePalette(Consumer<NoteColour> onPick) containing a row of 7 round colour buttons (custom paintComponent drawing a filled circle in the body colour with a darker outline); clicking calls onPick and closes the popup; void showBelow(Component anchor). Teach me: JPopupMenu.show, custom painting with Graphics2D.fillOval and antialiasing, Consumer<T> callbacks. Ask me how I'd loop over NoteColour.values() to build the buttons. Don't write it.

---

# PHASE 7: `NoteWindow` (assembly)

Everything you need now exists. This phase puts the pieces together **once**. The constructor is final from the start:

`public NoteWindow(Note note, NoteController controller, WindowActions actions)`

with `implements TopBarListener, BottomBarListener`.

### Step 7.1: Structure and background colour

**File:** `ui/NoteWindow.java` (**Class** extending `JFrame`)

**What this step does:** builds the empty frame with all regions and paints it in the note's colour. Interface methods stay as stubs (IntelliJ: Alt+Enter > *Implement methods*).

#### Why "the background uses a NoteColour", and how

A Swing window is a stack of layers, and **each layer paints its own background**:

```
JFrame (hidden behind the content pane, ignore its colour)
 └─ content pane (JPanel)          <- this is the visible "body"
     ├─ NORTH:  TopBar + FormatToolbar (each is a JPanel with its own colour)
     ├─ CENTER: layered stack: back drawing layer, scroll pane > RichTextArea, front drawing layer
     └─ SOUTH:  BottomBar
```

If a layer is **opaque** (the default for `JPanel`), it paints a solid colour over what is below, which is why gray strips appear when you only colour one layer. So the rule: colour the content pane, and make every child either the right colour or transparent (`setOpaque(false)`). The note's colour comes from `note.getColour()` (an enum), so you ask the enum for `getBody()` and `getHeader()`; nothing is hard-coded.

#### Build it

1. Fields: `Note note`, `NoteController controller`, `WindowActions actions`, `TopBar topBar`, `FormatToolbar formatToolbar`, `RichTextArea richText`, `JScrollPane scrollPane`, `DrawingLayer backLayer`, `DrawingLayer frontLayer`, `JLayeredPane layers`, `BottomBar bottomBar`, `ThemePalette palette`.
2. **First line of the constructor:** `setUndecorated(true)`. It must be called *before* the window is shown, otherwise Java throws an exception.
3. `setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)` (you will handle closing yourself).
4. Content pane: `JPanel root = (JPanel) getContentPane(); root.setLayout(new BorderLayout());`
5. NORTH: a panel with `BoxLayout(Y_AXIS)` holding `topBar` then `formatToolbar`; make it non-opaque.
6. CENTER (the layered stack):
   - `richText = new RichTextArea()`; `scrollPane = new JScrollPane(richText)`; then make all three see-through: `scrollPane.setOpaque(false)`, `scrollPane.getViewport().setOpaque(false)`, `scrollPane.setBorder(null)` (richText is already non-opaque).
   - `backLayer = new DrawingLayer(note.getStrokes(), true, false)` and `frontLayer = new DrawingLayer(note.getStrokes(), false, true)`.
   - `layers = new JLayeredPane()`. Add `backLayer` at `JLayeredPane.DEFAULT_LAYER`, `scrollPane` at `PALETTE_LAYER`, `frontLayer` at `MODAL_LAYER` (higher number is on top).
   - A `JLayeredPane` has **no layout manager**, so size the three components yourself: add a `ComponentListener` on `layers` whose `componentResized` calls `setBounds(0, 0, layers.getWidth(), layers.getHeight())` on each.
7. SOUTH: `bottomBar`.
8. `applyColour(NoteColour c)` private method: `root.setBackground(c.getBody())`, `topBar.applyColour(c)`, `bottomBar.applyColour(c)`, `formatToolbar.applyColour(c)`, `root.setBorder(BorderFactory.createLineBorder(c.getHeader(), 1))`, `repaint()`. Call it with `note.getColour()` in the constructor.
9. Set the size from the note temporarily (`setSize(note.getWidth(), note.getHeight())`); `setVisible(true)` is NOT called here, `WindowManager` does that.

**Test:** In `Sandbox`: make a `NoteController` with a `NoteRepository(Path.of("test.json"))`, `Note.createNew(100,100)`, a `WindowActions` stub (anonymous class, methods print). Show the window. You should see a yellow, borderless note with a top bar and bottom bar, and you can type in the middle. Gray strip? One of your panels is still opaque. White text area? The scroll pane viewport or the text area is still opaque. Change `note.setColour(NoteColour.BLUE)` and call `applyColour` to see it switch.

**Tutor prompt**

> STEP 7.1. File: ui/NoteWindow.java extends JFrame implements TopBarListener, BottomBarListener. Final constructor: NoteWindow(Note note, NoteController controller, WindowActions actions). Already built: TopBar (getDragHandle(), applyColour(NoteColour), setPinned(boolean)), FormatToolbar(RichTextArea), RichTextArea, DrawingLayer(List<InkStroke>, boolean paintsBehindText, boolean takesInput), BottomBar (getResizeGrip(), applyColour), ThemePalette. This step: setUndecorated(true) first; content pane with BorderLayout colouring from note.getColour().getBody(); NORTH = BoxLayout panel with TopBar + FormatToolbar; CENTER = JLayeredPane with back DrawingLayer (DEFAULT_LAYER), JScrollPane(RichTextArea) (PALETTE_LAYER), front DrawingLayer (MODAL_LAYER), all resized via a ComponentListener because JLayeredPane has no layout manager; SOUTH = BottomBar; a private applyColour(NoteColour) that colours the content pane, top/bottom bars, toolbar and a 1px border in the header colour. Teach me: the Swing layer stack (JFrame, root pane, content pane), opaque vs non-opaque and why gray strips appear, BorderLayout, BoxLayout, JLayeredPane z-order, why setUndecorated must come before setVisible. Ask me first which components I need to make non-opaque and why. Don't write the class.

### Step 7.2: Drag, resize, minimum size

**File:** `ui/NoteWindow.java` (continue)

**How it works**
- **Drag:** in `mousePressed` on `topBar.getDragHandle()` store the offset: `mouse screen position - window location` (`e.getLocationOnScreen()` and `getLocation()`). In `mouseDragged`, set `setLocation(mouseScreen - offset)`. Using *screen* coordinates avoids jitter, because the window itself moves under the mouse.
- **Resize:** same idea on `bottomBar.getResizeGrip()`: store the starting mouse screen position and starting size; on drag, `setSize(startW + dx, startH + dy)` clamped to a minimum.
- **Minimum size:** `setMinimumSize(new Dimension(200, 150))` plus clamp in your resize code (some platforms ignore it for undecorated windows).

**Test:** Drag the note by the top bar middle, resize by the bottom-right grip, try to shrink it below the minimum.

**Tutor prompt**

> STEP 7.2. File: ui/NoteWindow.java (continuing). Add dragging via topBar.getDragHandle() and resizing via bottomBar.getResizeGrip() for my undecorated JFrame, plus minimum size 200x150. Teach me: MouseAdapter pressed/dragged, getLocationOnScreen vs getPoint (why screen coordinates avoid jitter), computing deltas, clamping values, and why setMinimumSize can be ignored for undecorated windows. Ask me what I need to store in mousePressed for drag and for resize. Don't write the code.

### Step 7.3: Load from the note and save changes

**File:** `ui/NoteWindow.java` (continue)

**How it works**
- **Loading (constructor, before attaching listeners):** `setBounds(ScreenUtil.ensureVisible(new Rectangle(x, y, w, h), 200, 150))`; `richText.setRuns(note.getContent())`; `setAlwaysOnTop(note.isPinned())`; `topBar.setPinned(note.isPinned())`.
- **Why listeners come last:** attaching them before loading would trigger saves for every loaded property.
- **Saving:**
  - `ComponentListener` on the frame: `componentMoved` / `componentResized` copy `getX(), getY(), getWidth(), getHeight()` into the note and call `controller.updateNote(note, false)`.
  - `richText.addContentListener(...)`: `note.setContent(richText.getRuns())` then `controller.updateNote(note, true)`.
  - `frontLayer.setOnRedrawNeeded(() -> { backLayer.repaint(); frontLayer.repaint(); })` and `frontLayer.setOnStrokeFinished(() -> controller.updateNote(note, true))`.

**Test:** In `Sandbox`: create a window, type, move, resize, then call `controller.saveNow()` and read the JSON file. Recreate the window from `controller.getNote(id)` and it should look the same.

**Tutor prompt**

> STEP 7.3. File: ui/NoteWindow.java (continuing). Already built: NoteController.updateNote(Note, boolean contentChanged), ScreenUtil.ensureVisible(Rectangle,int,int), RichTextArea.getRuns()/setRuns()/addContentListener(Runnable), DrawingLayer callbacks setOnRedrawNeeded/setOnStrokeFinished. Add: load bounds, content, pinned state from the Note in the constructor (before attaching listeners); then attach a ComponentListener (moved/resized -> copy bounds to Note, updateNote(note,false)), a content listener (note.setContent(getRuns()), updateNote(note,true)), and the drawing callbacks (repaint both layers; on stroke finished updateNote(note,true)). Teach me: ComponentListener/ComponentAdapter, why attach listeners after loading, avoiding save loops. Ask me what would happen if I attached them first. Don't write the code.

### Step 7.4: Implement all button actions

**File:** `ui/NoteWindow.java` (continue)

Fill in the interface methods:

| Method | What it does |
|---|---|
| `onNewNote()` | `actions.newNote()` |
| `onOpenManager()` | `actions.openManager()` |
| `onClose()` | `actions.closeNote(note.getId())` |
| `onPinToggled(p)` | `setAlwaysOnTop(p)`; `note.setPinned(p)`; `controller.updateNote(note,false)` |
| `onColourClicked(anchor)` | create `ThemePalette` once with a callback that does `note.setColour(c)`, `applyColour(c)`, `controller.updateNote(note,true)`; then `palette.showBelow(anchor)` |
| `onDeleteRequested()` | `JOptionPane.showConfirmDialog(...)`; on Yes `actions.deleteNote(note.getId())` |
| `onFormatToggle()` | flip `formatToolbar.setVisible(...)`, then `revalidate()` / `repaint()` |
| `onChecklistClicked()` | `ChecklistSupport.insertCheckbox(richText)` |
| `onModeChanged(m)` | `frontLayer.setMode(m)`; set a crosshair cursor over the stack if drawing |
| `onPenColourChanged(c)` | `frontLayer.setPenColour(c)` |
| `onPenSizeChanged(s)` | `frontLayer.setPenWidth(s)` |
| `onBehindTextChanged(b)` | `frontLayer.setNewStrokesBehindText(b)` |
| `onClearDrawings()` | confirm; `note.getStrokes().clear()`; repaint both layers; `controller.updateNote(note,true)` |

Also in the constructor:
- `ChecklistSupport.install(richText)`
- Window close (Alt+F4): add a `WindowListener` whose `windowClosing` calls `onClose()`.
- Window-level shortcuts on `getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)`: Ctrl+N calls `onNewNote()`, Ctrl+Shift+L calls `onChecklistClicked()`.

**Test:** In `Sandbox`, click every button and check the console output from your `WindowActions` stub; change colour, pin, draw in Pen mode both in front of and behind text, erase, use the checklist, toggle the format bar.

**Tutor prompt**

> STEP 7.4. File: ui/NoteWindow.java (continuing). I implement TopBarListener and BottomBarListener now. Mapping: onNewNote -> actions.newNote(); onOpenManager -> actions.openManager(); onClose -> actions.closeNote(id); onPinToggled -> setAlwaysOnTop + note.setPinned + controller.updateNote(note,false); onColourClicked(anchor) -> ThemePalette callback sets colour, calls applyColour and updateNote(note,true); onDeleteRequested -> JOptionPane confirm then actions.deleteNote(id); onFormatToggle -> show/hide FormatToolbar and revalidate; onChecklistClicked -> ChecklistSupport.insertCheckbox(richText); onModeChanged/onPenColourChanged/onPenSizeChanged/onBehindTextChanged -> forward to the front DrawingLayer; onClearDrawings -> confirm, clear note strokes, repaint, save. Also: WindowListener windowClosing -> onClose(); window-wide key bindings with WHEN_IN_FOCUSED_WINDOW for Ctrl+N and Ctrl+Shift+L. Teach me: JOptionPane confirm dialogs, WindowListener, root pane InputMap with WHEN_IN_FOCUSED_WINDOW, revalidate vs repaint. Ask me which button should NOT mark the note's content as changed. Don't write the methods.

---

# PHASE 8: Manager, window manager, startup

### Step 8.1: `ManagerWindow`

**File:** `ui/ManagerWindow.java` (**Class** extending `JFrame`)

**Why now:** `WindowManager` creates it, so it must exist first. It needs only the controller and `WindowActions`, not `WindowManager` itself.

**How it works:** A normal window with a search box on top and a tabbed pane: **Notes** and **Trash**. Each tab shows a scrollable list (`JList` with a custom `ListCellRenderer` showing a colour chip, the first line of the note, and last edited date). It refreshes itself by registering `controller.addChangeListener(this::refresh)`.

**Build it**
- Constructor `ManagerWindow(NoteController controller, WindowActions actions)`
- Search `JTextField` + `DocumentListener` calls `refresh()` using `controller.search(text)`.
- Notes tab: double-click opens (`actions.openNote(id)`); buttons *Open*, *Delete* (confirm, then `actions.deleteNote(id)`), *New note* (`actions.newNote()`).
- Trash tab: *Restore* (`controller.restoreNote(id)`), *Delete forever* optional.
- Format dates with `DateTimeFormatter` from `updatedAt`.
- Close just hides/disposes it (does not exit the app).

**Test:** In `Sandbox`, with a controller holding 3 notes (one deleted).

**Tutor prompt**

> STEP 8.1. File: ui/ManagerWindow.java extends JFrame. Already built: NoteController (getAllNotes(), getDeletedNotes(), search(String), restoreNote(id), addChangeListener(Runnable)), WindowActions (newNote(), openNote(id), deleteNote(id)), Note.getPlainText(), getColour(), getUpdatedAt(). Spec: constructor ManagerWindow(NoteController, WindowActions); search JTextField filtering live; JTabbedPane with Notes and Trash tabs; each a JList with a custom ListCellRenderer (colour chip, first line of text, last-edited date); double-click opens the note; Open/Delete/New buttons; Restore in Trash; refreshes via controller.addChangeListener. Teach me: JList + DefaultListModel + ListCellRenderer, JTabbedPane, DocumentListener on a search box, MouseAdapter double-click, DateTimeFormatter. Ask me how the window will know when notes change. Don't write the class.

### Step 8.2: `WindowManager`

**File:** `controller/WindowManager.java` (**Class** implementing `WindowActions`)

**Why now:** All the windows exist; this class keeps track of them and holds the "restore what was open" logic.

**How it works:** It holds `Map<String, NoteWindow> windows`, one `ManagerWindow` (created on first use, reused after), and the `NoteController`.

**Build it**
- Constructor `WindowManager(NoteController controller)`
- `void restoreOpenNotes()`: open every note from `controller.getOpenNotes()`. If none are open, open the most recently edited note. If no notes exist at all, `controller.createNote()` and open it. **The manager is never opened here.**
- `openNote(id)`: if a window exists, bring it to front (`setState(NORMAL)`, `toFront()`, `requestFocus()`); otherwise create `new NoteWindow(note, controller, this)`, `note.setOpen(true)`, `controller.updateNote(note,false)`, `setVisible(true)`, store it.
- `newNote()`: `controller.createNote(x + 30, y + 30)` offset from the focused note (or default), then `openNote(id)`.
- `closeNote(id)`: `note.setOpen(false)`, `controller.updateNote(note,false)`, dispose and remove the window.
- `deleteNote(id)`: `controller.softDeleteNote(id)`, then dispose and remove the window.
- `openManager()`: create if null, `setVisible(true)`, `toFront()`.
- `showAllNotes()`: bring every open window forward (used by tray and second launch).
- `void shutdown()`: `controller.saveNow()` **without** setting any notes to closed, so quitting the app keeps the "open" state for next launch.

**Hints:** Closing the app must **not** loop through windows calling `closeNote`, or every note becomes "closed" and nothing is restored next launch. Without a tray icon, closing the last window ends the program; that is fine for now because the most recent note reopens next time.

**Test:** Run it through `Sandbox` or `Main`: restore opens your notes; the + button opens a new note; ☰ opens the manager; ✕ hides a note and the manager's *Open* button brings it back; delete moves it to Trash.

**Tutor prompt**

> STEP 8.2. File: controller/WindowManager.java implements WindowActions. Already built: NoteController (createNote(), createNote(x,y), getNote(id), getOpenNotes(), getAllNotes(), updateNote(Note,boolean), softDeleteNote(id), saveNow()), NoteWindow(Note, NoteController, WindowActions), ManagerWindow(NoteController, WindowActions). Spec: Map<String,NoteWindow>; restoreOpenNotes() (open all notes with open==true; if none, open the most recently edited; if no notes exist, create one; never opens the manager); openNote(id) (bring to front if already open); newNote() (offset 30px from the focused note); closeNote(id) (sets open=false, updates, disposes); deleteNote(id) (soft delete, dispose); openManager() (lazy, reused); showAllNotes(); shutdown() (saveNow without marking notes closed). Teach me: Map of windows, toFront/requestFocus, lazy creation, why quitting must not mark notes closed. Ask me how I'd offset a new note from the current one. Don't write the class.

### Step 8.3: Final `Main` and full test

**File:** `Main.java` (replace the temporary code)

**How it works / Build it (in this order inside `invokeLater`)**
1. `FlatLightLaf.setup()`.
2. Create `NoteRepository(NoteRepository.defaultPath())`, `NoteController`, `WindowManager`.
3. `SingleInstanceLock.acquire(windowManager::showAllNotes)`; if it returns `false`, `System.exit(0)`.
4. `controller.purgeOldTrash(30)`.
5. `windowManager.restoreOpenNotes()`.

**Full test checklist**
- First run: one blank yellow note appears, no manager.
- Type text, format it, draw, make a checklist, change colour, pin, move and resize.
- Click +, create two more notes; drag them apart.
- Close the app, run again: all three notes are in the same places with the same content and drawings.
- Close all notes with ✕, run again: the most recent one returns.
- ☰ opens the manager; search works; delete goes to Trash; restore brings it back.
- Run the app twice: the second copy exits and the first shows its notes.
- Edit the JSON so a note has `"x": 9999`; it appears on screen.

**Tutor prompt**

> STEP 8.3. File: Main.java. Already built: NoteRepository.defaultPath(), NoteController, WindowManager (restoreOpenNotes(), showAllNotes()), SingleInstanceLock.acquire(Runnable). Spec: inside SwingUtilities.invokeLater: FlatLightLaf.setup(); build repository, controller, window manager; if !SingleInstanceLock.acquire(windowManager::showAllNotes) exit; controller.purgeOldTrash(30); windowManager.restoreOpenNotes(). Teach me: startup ordering, method references (this::method), and why the single-instance check must happen before loading data. Ask me what could corrupt notes.json if two copies ran. Short answer.

---

# PHASE 9: Polish and system integration

### Step 9.1: `TrayManager`

**File:** `ui/TrayManager.java` (**Class**)

**Build it:** `TrayManager(NoteController c, WindowManager w)` and `void install()`. Use `SystemTray.isSupported()`, `SystemTray.getSystemTray().add(new TrayIcon(image, "StickyLite", popup))`. Menu: *Show all notes* (`w.showAllNotes()`), *New note* (`w.newNote()`), *Open manager* (`w.openManager()`), *Exit* (`w.shutdown()` then `System.exit(0)`). Create a 16x16 icon with `BufferedImage` drawn in code.

**Check:** After installing the tray icon, close all notes and confirm the process is still running in Task Manager. If the program exits when the last window closes, keep it alive with a hidden helper (look up "Java keep AWT alive with no windows").

**Tutor prompt**

> STEP 9.1. File: ui/TrayManager.java. Spec: constructor TrayManager(NoteController, WindowManager); install() adds a TrayIcon (16x16 BufferedImage drawn in code) with a PopupMenu: Show all notes, New note, Open manager, Exit (windowManager.shutdown() then System.exit(0)). Teach me: SystemTray/TrayIcon/PopupMenu (AWT, not Swing), SystemTray.isSupported(), and whether the JVM stays alive when all windows are closed but a tray icon exists. Ask me how Exit must save. Don't write the class.

### Step 9.2: Save on exit

**Build it:** In `Main`, `Runtime.getRuntime().addShutdownHook(new Thread(controller::saveNow))`. This protects against the final 500 ms of debounced typing being lost if the process is stopped. It cannot run if the process is force-killed.

**Tutor prompt**

> STEP 9.2. In Main add a JVM shutdown hook that calls controller.saveNow(). Teach me what a shutdown hook is, when it runs and when it doesn't, and thread-safety concerns of saving from another thread while Swing runs. Ask me what could go wrong. Short answer.

### Step 9.3: Start with Windows

**Build it:** Package the app with `jpackage` (JDK tool) as an app image or installer, then put a shortcut to the `.exe` in the Startup folder (press Win+R, type `shell:startup`). Optionally add a "Start with Windows" checkbox in the tray menu that creates or deletes the shortcut.

**Tutor prompt**

> STEP 9.3. I want my Maven Java 17 Swing app to start at Windows login. Compare: a shortcut in shell:startup vs a registry Run key, and explain how to produce an exe with jpackage (what the options mean, what tools it needs). Ask me which option I prefer and why before going deeper. Short answer.

### Step 9.4: Optional extras (pick any, in any order)

Real icons instead of symbols, collapse a note to its title bar, note transparency slider, export/backup to a zip, FlatLaf dark mode, image paste, drawings that scroll with the text.

**Tutor prompt**

> STEP 9.4. StickyLite core is done. I want to add: [name ONE feature]. Based on my class layout (model, storage, controller, util, ui as listed in my setup message), ask me guiding questions to help me decide which classes to change and which new fields (if any) the Note model needs. Don't write code.

### Step 9.5: Cleanup

Delete `Sandbox.java`, remove any temporary test code in `Main`, run a last full test from Step 8.3, and commit.

---

# Master order (checklist)

| Step | File | Type |
|---|---|---|
| 0.1 | `pom.xml`, `Main.java` | setup |
| 0.2 | `Sandbox.java` | scratch |
| 1.1 | `model/NoteColour` | enum |
| 1.2 | `model/TextRun` | class |
| 1.3 | `model/InkStroke` | class |
| 1.4 | `model/Note` | class |
| 1.5 | `model/AppState` | class |
| 2.1 | `storage/NoteRepository` | class |
| 3.1 | `controller/NoteController` | class |
| 4.1 | `util/ScreenUtil` | class |
| 4.2 | `util/SingleInstanceLock` | class |
| 4.3 | `util/TextAttrs` | class |
| 5.1 | `WindowActions`, `TopBarListener`, `BottomBarListener`, `DrawMode` | interfaces, enum |
| 6.1 | `ui/TopBar` | class |
| 6.2 | `util/TextRunConverter` | class |
| 6.3 | `ui/RichTextArea` | class |
| 6.4 | `ui/FormatToolbar` | class |
| 6.5 | `util/ChecklistSupport` | class |
| 6.6 | `ui/BottomBar` | class |
| 6.7 | `ui/DrawingLayer` | class |
| 6.8 | `ui/ThemePalette` | class |
| 7.1 to 7.4 | `ui/NoteWindow` | class (4 passes: structure, drag/resize, load/save, actions) |
| 8.1 | `ui/ManagerWindow` | class |
| 8.2 | `controller/WindowManager` | class |
| 8.3 | `Main` | final startup |
| 9.1 to 9.5 | `ui/TrayManager`, shutdown hook, autostart, extras, cleanup | polish |

## Features covered and where

Opens notes first, not the manager (8.2, 8.3) · restores previous state (7.3, 8.2) · `[]` checklist (6.5) · draw on top of or behind text (6.7, 7.1) · colour themes (1.1, 6.8) · top-left manager and top-right + (6.1) · bottom checklist button (6.6) · bold, italic, font, size (6.3, 6.4) · auto-save (3.1) · pin on top (7.4) · off-screen protection and minimum size (4.1, 7.2, 7.3) · delete confirmation and trash (7.4, 8.1) · undo/redo and shortcuts (6.3, 7.4) · search (3.1, 8.1) · tray icon (9.1) · start with Windows (9.3) · single instance (4.2, 8.3).

## Saving tokens

- Do one step per chat; start each chat with the setup message above.
- Paste only the class you're working on plus the exact error text.
- Try for 15 minutes before using a prompt.
- Commit after each working step.
