# Build Workflow

## Overview

This document describes the build and testing workflow for Futtoboru.

## Build Scripts

### `build.bat`
- **Purpose:** Builds the project using Maven
- **Usage:** `build.bat`
- **What it does:**
  - Runs `mvn clean install -DskipTests`
  - Creates the JAR file in `futtoboru-desktop/target/`
  - Shows build status (success/failure)

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
- Maven should be in your PATH, OR
- Set `MAVEN_HOME` environment variable, OR
- Edit `build.bat` to use: `C:\apache-maven-3.6.3\bin\mvn.cmd`

## Build Output

After a successful build:
- JAR file: `futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar`
- Compiled classes: `futtoboru-core/target/classes/`
- Test classes: `futtoboru-core/target/test-classes/`

## Troubleshooting

### "Maven not found"
- Add Maven to your PATH
- Or set MAVEN_HOME environment variable
- Or edit `build.bat` to use full path to Maven

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

*Last updated: 2025-12-14*

