# 🧗 Climbing Tracker

A Java + SQLite desktop app for tracking rock climbing records.
Built for the Year 11 Computer Science Semester Two Project.

---

## Project Structure

```
climbing-tracker/
├── src/main/java/com/climbing/
│   ├── ClimbingTrackerApp.java   ← Main GUI (Swing)
│   ├── DatabaseManager.java      ← All SQL lives here (see sql/queries.sql)
│   ├── Climb.java                ← Data model
│   └── Validator.java            ← Input validation
│
├── sql/
│   └── queries.sql               ← ★ All SQL statements with full comments
│
├── lib/
│   └── sqlite-jdbc-*.jar         ← Place the SQLite JDBC driver here
│
└── .vscode/
    ├── launch.json
    └── settings.json
```

---

## Setup (VS Code)

### 1. Install Extensions
Install the **Extension Pack for Java** from the VS Code Marketplace.

### 2. Download the SQLite JDBC Driver
- Go to https://github.com/xerial/sqlite-jdbc/releases
- Download `sqlite-jdbc-X.X.X.jar` (latest version)
- Create a `lib/` folder in this project and place the `.jar` file inside it

### 3. Add the JAR to the Classpath
In VS Code, open the Command Palette (`Cmd+Shift+P` / `Ctrl+Shift+P`) and run:
```
Java: Configure Classpath
```
Under **Referenced Libraries**, click **+** and select the `.jar` in `lib/`.

Alternatively, edit `.vscode/settings.json` — the `lib/**/*.jar` glob will auto-detect it.

### 4. Run
Press **F5** or use the Run panel to launch `ClimbingTrackerApp`.

The database file `climbing_tracker.db` will be created in the project root automatically.

---

## SQL Reference

All SQL statements are in two places:

| File | Purpose |
|------|---------|
| `sql/queries.sql` | Full annotated versions — read this to understand each query |
| `DatabaseManager.java` | Java `String` constants — each one labelled with its `SQL_BLOCK` name |

The `SQL_BLOCK` comments in `DatabaseManager.java` match the section headers in
`queries.sql`, making it easy to find the corresponding SQL for each operation.

---

## Features

| Feature | Design Spec # |
|---------|--------------|
| Enter grade (V0–V17) | 2 |
| Enter date (validated, no future dates) | 3 |
| Enter location (max 100 chars) | 4 |
| Indoors / Outdoors dropdown | 5 |
| Climb type (preset + custom) | 6 |
| Sort & filter by any field | 7 |
| Entries saved to SQLite database | 8 |
| Delete entries | — |
| Summary statistics tab | — |

---

## Notes on AI Usage

- The GUI layout and colour palette were assisted by Claude.
- All SQL statements in `sql/queries.sql` were written manually.
- Validation logic in `Validator.java` was written manually.
# CSY11T2Project
