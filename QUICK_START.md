# Quick Start Guide

## Prerequisites
- **Java 17 or higher** (tested with Java 21)
- **Maven 3.6+** (or Maven wrapper)

## Building the Game

```bash
# Clean and build
mvn clean install

# Or skip tests for faster build
mvn clean install -DskipTests
```

## Running the Game

```bash
# After building, run the JAR
java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.3.0-SNAPSHOT-jar-with-dependencies.jar
```

## First Run

1. **Main Menu** will appear
2. Click **"New Game"**
3. Select a season (currently only 1888-1889 available)
4. Select countries/leagues
5. Create or load a manager
6. Start the game

## Troubleshooting

### Game won't start / Null pointer exception
- Make sure you've started a **New Game** before trying to continue
- The game requires a SaveGame to be created first

### Build fails
- Check Java version: `java -version` (needs 17+)
- Check Maven: `mvn --version`
- Try cleaning: `mvn clean`

### JAR file not found
- Make sure build completed successfully
- Check: `futtoboru-desktop/target/futtoboru-desktop-0.3.0-SNAPSHOT-jar-with-dependencies.jar`

## Current Status

✅ **Working:**
- Project builds successfully
- Game starts and shows menu
- New game creation works
- Basic navigation works

⚠️ **Known Issues:**
- Match simulation not implemented (matches can't be played yet)
- Some screens incomplete
- Save/load needs testing

---

*Last Updated: 2025-12-14*

