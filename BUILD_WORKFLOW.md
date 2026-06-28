# Build Workflow

## Overview

This document describes the build and testing workflow for Futtoboru.

## Environment check

Run **`check-env.bat`** from the repo root. It reports Java, Maven, JAR path, and whether the game is already compiled.

Shared setup lives in **`scripts/env.bat`** (Java 17+, Maven from PATH / `MAVEN_HOME` / standard install paths).

## Build Scripts

### `build.bat`
- **Purpose:** Builds the project using Maven
- **Usage:** `build.bat`
- **What it does:**
  - Loads `scripts/env.bat` (repo root, Java, Maven)
  - Runs `generate-build-info.bat`
  - Runs `mvn clean install -DskipTests`
  - Verifies the JAR exists in `futtoboru-desktop/target/`

### `build-and-run.bat`
- **Purpose:** Builds the project and then runs it
- **Usage:** `build-and-run.bat`
- **What it does:**
  1. Calls `build.bat` to build the project
  2. If build succeeds, calls `run-game-debug.bat` to run the game

### `run-game-debug.bat`
- **Purpose:** Runs the game in debug mode
- **Usage:** `run-game-debug.bat`
- **What it does:**
  - Checks if JAR file exists
  - If not found, prompts to build
  - Runs the game with debug logging
  - Saves logs to `game-debug.log`

## Recommended Workflow

### For Development & Testing:

1. **Make code changes**
2. **Build the project:**
   ```bash
   build.bat
   ```
   OR use the combined script:
   ```bash
   build-and-run.bat
   ```

3. **Test the game**
4. **Check logs** in `game-debug.log` if issues occur

### Quick Testing:

If you just want to run the game and it's already built:
```bash
run-game-debug.bat
```

The script will automatically detect if a rebuild is needed.

## Maven Requirements

- **Maven 3.6+** required
- Maven on PATH (recommended), or `MAVEN_HOME`, or install via [scripts/install-maven-windows.ps1](scripts/install-maven-windows.ps1)
- Scripts auto-detect `C:\Program Files\Apache\Maven\apache-maven-3.9.16` if present

## Build Output

After a successful build:
- JAR file: `futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar`
- Compiled classes: `futtoboru-core/target/classes/`
- Test classes: `futtoboru-core/target/test-classes/`

## Troubleshooting

### "Maven not found"
- Run `check-env.bat` for a clear error
- Add Maven to PATH or set `MAVEN_HOME`
- Or run `scripts\install-maven-windows.ps1` as Administrator (see [scripts/README.md](scripts/README.md))

### "JAR file not found"
- Run `build.bat` first
- Check that build completed successfully
- Verify JAR exists in `futtoboru-desktop/target/`

### Build fails
- Check Java version (requires Java 17+)
- Check Maven version (requires 3.6+)
- Review error messages in console
- Check `pom.xml` for dependency issues

---

*Last updated: 2026-06-28*

