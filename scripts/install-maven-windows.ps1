#Requires -RunAsAdministrator
<#
.SYNOPSIS
  Installs Apache Maven (latest stable 3.9.x) for all users on Windows.

.DESCRIPTION
  - Downloads official binary zip from dlcdn.apache.org
  - Installs under C:\Program Files\Apache\Maven\
  - Sets MAVEN_HOME (Machine) and adds %MAVEN_HOME%\bin to Machine PATH
#>

$ErrorActionPreference = 'Stop'

$MavenVersion = '3.9.16'
$InstallRoot = 'C:\Program Files\Apache\Maven'
$MavenHome = Join-Path $InstallRoot "apache-maven-$MavenVersion"
$DownloadUrl = "https://dlcdn.apache.org/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
$TempZip = Join-Path $env:TEMP "apache-maven-$MavenVersion-bin.zip"
$LogFile = Join-Path $env:TEMP "futtoboru-maven-install.log"

function Write-Log {
    param([string]$Message)
    $line = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') $Message"
    Add-Content -Path $LogFile -Value $line
    Write-Host $line
}

try {
    Write-Log "Starting Maven $MavenVersion install for all users"

    if (-not (Test-Path $InstallRoot)) {
        New-Item -ItemType Directory -Path $InstallRoot -Force | Out-Null
    }

    Write-Log "Downloading $DownloadUrl"
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $TempZip -UseBasicParsing -TimeoutSec 300

    $extractTemp = Join-Path $env:TEMP "apache-maven-$MavenVersion-extract"
    if (Test-Path $extractTemp) {
        Remove-Item -Recurse -Force $extractTemp
    }
    Expand-Archive -Path $TempZip -DestinationPath $extractTemp -Force

    $extractedFolder = Join-Path $extractTemp "apache-maven-$MavenVersion"
    if (-not (Test-Path $extractedFolder)) {
        throw "Extracted folder not found: $extractedFolder"
    }

    if (Test-Path $MavenHome) {
        Write-Log "Removing previous install at $MavenHome"
        Remove-Item -Recurse -Force $MavenHome
    }

    Move-Item -Path $extractedFolder -Destination $MavenHome
    Remove-Item -Recurse -Force $extractTemp -ErrorAction SilentlyContinue
    Remove-Item -Force $TempZip -ErrorAction SilentlyContinue

    Write-Log "Installed to $MavenHome"

    [Environment]::SetEnvironmentVariable('MAVEN_HOME', $MavenHome, 'Machine')
    [Environment]::SetEnvironmentVariable('M2_HOME', $MavenHome, 'Machine')

    $machinePath = [Environment]::GetEnvironmentVariable('Path', 'Machine')
    $mavenBin = Join-Path $MavenHome 'bin'

    if ($machinePath -notlike "*$mavenBin*") {
        if ([string]::IsNullOrWhiteSpace($machinePath)) {
            $newPath = $mavenBin
        } else {
            $newPath = "$machinePath;$mavenBin"
        }
        [Environment]::SetEnvironmentVariable('Path', $newPath, 'Machine')
        Write-Log "Added $mavenBin to Machine PATH"
    } else {
        Write-Log "Maven bin already on Machine PATH"
    }

    # Refresh current process PATH for verification in same elevated session
    $env:MAVEN_HOME = $MavenHome
    $env:M2_HOME = $MavenHome
    $env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')

    $mvn = Join-Path $mavenBin 'mvn.cmd'
    & $mvn -version | ForEach-Object { Write-Log $_ }

    Write-Log 'Maven install completed successfully'
    exit 0
} catch {
    Write-Log "ERROR: $($_.Exception.Message)"
    exit 1
}
