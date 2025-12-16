# Futtoboru
Futtoboru is an Open Source Alternative to the Football Manager Series

## Requirements
- Java 17 or higher
- Maven 3.6+ (or use Maven wrapper if available)

## Build Instructions

### Option 1: Using Build Scripts (Recommended for Windows)

**Build only:**
```bash
build.bat
```

**Build and run:**
```bash
build-and-run.bat
```

**Run only (auto-checks if build needed):**
```bash
run-game-debug.bat
```

### Option 2: Using Maven Directly

```bash
mvn clean install
```

## Run Instructions

### From Command Line:

```bash
java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar
```

**Note:** The JAR file is created in `futtoboru-desktop/target/` directory after building.

### Using Scripts:

- `run-game-debug.bat` - Runs the game in debug mode (checks if build needed)
- `build-and-run.bat` - Builds and runs the game automatically

## Development

The project uses GitFlow workflow. See `GITFLOW_WORKFLOW.md` for details.

- **Main branch:** `main` (production)
- **Development branch:** `develop` (current development)