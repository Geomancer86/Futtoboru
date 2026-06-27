# Windows tooling scripts

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
