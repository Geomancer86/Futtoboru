# Windows tooling scripts

## Dev environment (`env.bat`)

All root `.bat` launchers call **`scripts/env.bat`**, which:

- Sets repo root and `JAR_FILE` path
- Finds Java 17+ (PATH, `JAVA_HOME`, or common install dirs)
- Finds Maven (`mvn` on PATH, `MAVEN_HOME`, or `C:\Program Files\Apache\Maven\...`)

From repo root, run **`check-env.bat`** to print Java/Maven/JAR status before building.

## Apache Maven (system install)

**Script:** [install-maven-windows.ps1](install-maven-windows.ps1)

Installs **Apache Maven 3.9.16** (latest stable) for **all users**:

| Setting | Value |
|---------|--------|
| Install dir | `C:\Program Files\Apache\Maven\apache-maven-3.9.16` |
| `MAVEN_HOME` | Machine env var → install dir |
| `M2_HOME` | Same (legacy alias) |
| `PATH` | `%MAVEN_HOME%\bin` appended to **Machine** PATH |

### Run (requires Administrator)

Right-click PowerShell → **Run as administrator**, then:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
& "C:\path\to\futtoboru\scripts\install-maven-windows.ps1"
```

Or from repo root (UAC prompt):

```powershell
Start-Process powershell -Verb RunAs -Wait -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-File','scripts\install-maven-windows.ps1'
```

Log file: `%TEMP%\futtoboru-maven-install.log`

### Verify (new terminal after install)

```powershell
mvn -version
cd C:\path\to\futtoboru
mvn clean test
```

**Note:** Open a **new** terminal so updated Machine PATH is picked up.
