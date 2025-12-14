# Futtoboru
Futtoboru is an Open Source Alternative to the Football Manager Series

## Requirements
- Java 17 or higher
- Maven 3.6+ (or use Maven wrapper if available)

## Build Instructions

### Desktop:

```bash
mvn clean install
```

## Run Instructions

### From Command Line:

```bash
java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar
```

**Note:** The JAR file is created in `futtoboru-desktop/target/` directory after building.

## Development

The project uses GitFlow workflow. See `GITFLOW_WORKFLOW.md` for details.

- **Main branch:** `main` (production)
- **Development branch:** `develop` (current development)